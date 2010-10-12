package com.vegalabs.amail.client.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.vegalabs.general.client.utils.VegaUtils;
import com.vegalabs.amail.client.constants.ConstantsImpl;
import com.vegalabs.amail.client.constants.MessagesImpl;
import com.vegalabs.amail.client.resources.GlobalResources;
import com.vegalabs.amail.client.service.IService;
import com.vegalabs.amail.client.text.RichTextToolbar;
import com.vegalabs.amail.client.utils.ClientMailUtils;
import com.vegalabs.amail.shared.ActivityType;
import com.vegalabs.amail.shared.UUIDUtil;
import com.vegalabs.amail.shared.UnicodeString;

public class MailForm extends Composite {

	private static MailFormUiBinder uiBinder = GWT
			.create(MailFormUiBinder.class);

	interface MailFormUiBinder extends UiBinder<Widget, MailForm> {
	}
	
	
	@UiField TextBox toBox;
	@UiField VerticalPanel oracleHolderCntPnl;
	@UiField TextBox subjectBox;
	@UiField TextBox fromBox;
	@UiField RichTextArea contentRichTextArea;
	@UiField CaptionPanel contentRichTextAreaCptnPnl;
	
	@UiField Button btn1;
	@UiField Button btn2;
	@UiField Button btn3;
	@UiField Image img0;
	@UiField SimplePanel richTextToolbarPanel;
	@UiField VerticalPanel loadingPanel;
	@UiField VerticalPanel mainMailPanel;
	@UiField Anchor hideContentAnchor;
	@UiField Anchor saveContentAnchor; 
	@UiField Anchor forumLinkAnchor; 
	@UiField Anchor installerLinkAnchor; 
	
	
	SuggestBox suggestCntBox;
	
	@UiField
	Button importContactsBtn;
	
	
	String concatinatedContacts = null;
	
	
	protected VegaUtils utils;
	protected GlobalResources resources;
	protected ConstantsImpl constants;
	protected MessagesImpl messages;
	protected IService service;
	
	protected Map<String,String> activitiesMap = new HashMap<String,String>();
	
	private String bodyHtmlBackup = "";
	private String subjectBackup = "";
	
	@Inject
	public MailForm(final VegaUtils utils, final GlobalResources resources, final IService service, final ConstantsImpl constants, final MessagesImpl messages) {
		initWidget(uiBinder.createAndBindUi(this));
		resources.globalCSS().ensureInjected();
		
		this.utils = utils;
		this.resources = resources;
		this.constants = constants;
		this.service = service;
		
		img0.setVisible(false);
		mainMailPanel.setVisible(false);
		btn2.setVisible(false);
		btn3.setVisible(false);
		
		RichTextToolbar richTextToolbar = new RichTextToolbar(contentRichTextArea);
		richTextToolbarPanel.add(richTextToolbar);
		
		contentRichTextArea.setStylePrimaryName(resources.globalCSS().whiteRow());
		
		
//		initState();//XXX remove
		
		// start sendBtn.addClickHandler
		btn1.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(btn1.getText().equals(constants.replyStr())){
					replyBtnClicked();

				}else if(btn1.getText().equals(constants.forwardStr() )){
					forwardBtnClicked();
					
				}else if(btn1.getText().equals(constants.sendStr() )){
					final String recipients = toBox.getText();
					if("".equals(recipients) || recipients == null){
						utils.alert(constants.noRecipientWarningStr());
						event.preventDefault();
						return;
					}
					int activityType = ActivityType.NEW;
					String activity = activitiesMap.get("activity");
					if(activity.contains("reply")){
						activityType = ActivityType.REPLY;
						activitiesMap.put("activity", "done/reply");
					}else if(activity.contains("new")){
						activitiesMap.put("activity", "done/new");
						activityType = ActivityType.NEW;
					}else if(activity.contains("forward")){
						activitiesMap.put("activity", "done/forward");
						activityType = ActivityType.FORWARD;
					}
					
					final String sender = constructUserMailAddress();
					final String senderName = utils.retrUserName();
					
					String subject = subjectBox.getText();
					
					subject = encode(subject);
					subjectBackup = subject;
					
					String msgBody = contentRichTextArea.getHTML();
					bodyHtmlBackup = msgBody;
					
					msgBody = encode(msgBody);
					String recipientsTmp = encode(recipients);
					String senderTmp = encode(sender);
					String senderNameTmp = encode(senderName);
					
					String uuid = utils.retrFromState("uuid");

					
					Log.info("Sending Subject: " + subjectBackup + ", MsgBody: " + bodyHtmlBackup);
					String waveId = utils.retrWaveId();
					String iconUrl = utils.retrUserThumbnailUrl();
					String blipId = utils.retrFromState("blipId");
					if(blipId == null){
						blipId = "none";
					}
					try {
						img0.setVisible(true);
						utils.showStaticMessage(constants.sendingEmailStr());
						service.sendEmail(recipientsTmp,subject,msgBody,senderTmp,senderNameTmp,activityType,waveId,blipId,uuid,iconUrl, new AsyncCallback<JSONValue>() {

							@Override
							public void onSuccess(JSONValue result) {
								Log.info("Entering  onSuccess: " + result.toString());
								utils.dismissStaticMessage();
								
								img0.setVisible(false);
								String activity = activitiesMap.get("activity");
								String msgBodyForState = null;
								if(activity.contains("reply")){
									msgBodyForState = "<br><span style=\"font-size: 8pt; color:#B8B8B8\"><i>" + messages.infoReplySent(new Date(),recipients) +"</i></span><br>" + bodyHtmlBackup;
								}else if(activity.contains("new")){
									msgBodyForState = bodyHtmlBackup;
								}else if(activity.contains("forward")){
									msgBodyForState = "<br><span style=\"font-size: 8pt; color:#B8B8B8\"><i>" + messages.infoForwardSent(new Date(),recipients) +"</i></span><br>" + bodyHtmlBackup;
								}
								Log.info("Activity is: " + activity);
								try{
									Log.info("msgBodyForState before encode: " + msgBodyForState);
									
									msgBodyForState = encode(msgBodyForState);
									Log.info("msgBodyForState after encode: " + msgBodyForState);
									
									HashMap<String,String> delta = new HashMap<String, String>();
									delta.put("to",recipients);
									delta.put("from",senderName + "<" + sender + ">");
									if(activity.equals("done/reply")){
										delta.put("mode","READ");
									}else if(activity.equals("done/new")) {
										delta.put("mode","SENT");
									}
									delta.put("msgBody",msgBodyForState);
									delta.put("subject", subjectBackup);
									utils.putToState(delta);
									utils.showSuccessMessage(constants.emailSentStr(), 5);
								}catch(Exception e){
									utils.dismissAllStaticMessages();
									Log.error(msgBodyForState, e);
								}
//								cancelReply(); //XXX remove after verification 
								//robot should insert as reply new gadget with reply content
							}

							@Override
							public void onFailure(Throwable caught) {
								utils.dismissAllStaticMessages();
								utils.alert(caught.getMessage());
								img0.setVisible(false);
							}
						});
						utils.reportEvent("/sendEmail/","emailActions", utils.retrUserId(), 1);
					} catch (RequestException e) {
						utils.dismissAllStaticMessages();
						Log.error("recipients: " + recipients + ", " + subject + ", msgContent: " + msgBody + ", sender: " + sender, e);
					}
				}

			}

		});// end sendBtn.addClickHandler
		
		btn2.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(constants.forwardStr().equals(btn2.getText())){
					forwardBtnClicked();
				}else if(constants.cancelStr().equals(btn2.getText())){
					cancelAction();
				}
				
			}
		});
		
		btn3.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				 if(constants.cancelStr().equals(btn3.getText())){
					cancelAction();
				}
				
			}
		});
		
		hideContentAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String isHideContentStr = utils.retrFromPrivateSate("isHideContent");
				
				boolean isHideContent = !Boolean.parseBoolean(isHideContentStr);
				setUpIsHideContent(isHideContent);
				utils.putToPrivateSate("isHideContent",String.valueOf(isHideContent));
				event.preventDefault();
				utils.reportEvent("/hideContent/","editingActions", utils.retrUserId(), 1);
			}
		});
		
		saveContentAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				writeToStateIntoGadget();
				event.preventDefault();
				utils.showSuccessMessage(constants.contentSaveSuccess(), 5);
				utils.reportEvent("/saveDraft/","editingActions", utils.retrUserId(), 1);
			}
		});
		
		forumLinkAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				utils.requestNavigateTo(constants.discussWavyEmailUrl(), null);
				event.preventDefault();
				utils.reportEvent("/discuss/","auxillary", utils.retrUserId(), 1);
			}
		});
		
		installerLinkAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				utils.requestNavigateTo(constants.installWavyEmailUrl(), null);
				event.preventDefault();
				utils.reportEvent("/install/","auxillary", utils.retrUserId(), 1);
			}
		});
		
		
		
		
		Timer mailPnlTimer = new Timer() {
			@Override
			public void run() {
				String uuid = utils.retrFromState("uuid");
				if(uuid == null){
					uuid = UUIDUtil.genUUID();
					HashMap<String,String> delta = new HashMap<String, String>();
					delta.put("uuid",uuid);
					utils.putToState(delta);
				}

				String contactsKey = "contacts#" + utils.retrUserId();
				String contactsFromState = utils.retrFromState(contactsKey);
				
				if(contactsFromState != null){
					utils.putToPrivateSate(contactsKey, contactsFromState);
					utils.putToState(contactsKey, null);
				}
				if(contactsFromState == null){
					contactsFromState = utils.retrFromPrivateSate(contactsKey);
				}
				
				if(contactsFromState == null){
					Log.info("contacts are empty - sending request to load contacts");
					loadContacts();
				}else{
					concatinatedContacts = contactsFromState;
					Log.info("contactsFromState: " + contactsFromState);
					loadingPanel.setVisible(false);
					mainMailPanel.setVisible(true);
					init();
				}
			
			}
		};
		mailPnlTimer.schedule(1200);
		
		//run on state update - after sent/reply
		utils.addRunOnStateEventUpdate(new Runnable() {
			
			@Override
			public void run() {
				Log.info("addRunOnStateEventUpdate");
				cancelAction();//restore gadget from state
			}
		});
		
		
		importContactsBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(importContactsBtn.getText().equals(constants.importCnts())){
					Window.open("http://" + constants.appDomain() + ".appspot.com/LoginServlet?user=" + utils.retrUserId(), "", "");
//					Window.open("http://localhost:8888/LoginServlet?user=" + utils.retrHostId(), "", "");
					utils.putToState("contacts#" + utils.retrUserId(),null );
					concatinatedContacts = null;
					importContactsBtn.setText(constants.refreshContacts());
				}else if(importContactsBtn.getText().equals(constants.refreshContacts())){
					loadContacts();
				}
			}
		});
	}
	

	protected void loadContactsAndContent(String personId, String emailEventId) {
		try {
			Log.info("loadContactsAndContent: " + "personId: " + personId + ", emailEventId: " + emailEventId);
			service.loadContactsAndContent(personId, emailEventId, new LoadContactsAndContentAsyncCallback());
		} catch (RequestException e) {
			Log.error("personId: " + personId + ", emailEventId: " + emailEventId , e);
		}
		
	}


	int failuresCount = 0;
	private boolean isFirstInit= true;
	
	private class LoadContactsAsyncCallback implements AsyncCallback<JSONValue> {
		
		@Override
		public void onSuccess(JSONValue result) {
			utils.dismissStaticMessage();
			utils.showSuccessMessage(constants.doneStr(), 3);
			failuresCount = 0;
			String contacts = result.isObject().get("contacts").isString().stringValue();
			concatinatedContacts = decode(contacts);
			utils.putToPrivateSate("contacts#" + utils.retrUserId(), concatinatedContacts);
			loadingPanel.setVisible(false);
			mainMailPanel.setVisible(true);
			init();
			img0.setVisible(false);
		}
		
		@Override
		public void onFailure(Throwable caught) {
			Log.warn ("loadContacts userId: " + utils.retrUserId(), caught);
			failuresCount++;
			if(failuresCount < 2){
				loadContacts();
			}else{
				utils.dismissStaticMessage();
				utils.alert(caught.getMessage());
				utils.putToPrivateSate("contacts#" + utils.retrUserId(), "");
				loadingPanel.setVisible(false);
				mainMailPanel.setVisible(true);
				init();
			}
			img0.setVisible(false);
		}
	}
	
private class LoadContactsAndContentAsyncCallback implements AsyncCallback<JSONValue> {
		
		@Override
		public void onSuccess(JSONValue result) {
			failuresCount = 0;
			String contacts = result.isObject().get("contacts").isString().stringValue();
			concatinatedContacts = contacts;
			utils.putToPrivateSate("contacts#" + utils.retrUserId(), concatinatedContacts);
			
			String msgBody = result.isObject().get("msgBody").isString().stringValue();
			String subject = result.isObject().get("subject").isString().stringValue();
			String from = result.isObject().get("from").isString().stringValue();
			String toAll = result.isObject().get("toAll").isString().stringValue();
			String to = result.isObject().get("to").isString().stringValue();
			
			Log.info("MsgBody from API: " + msgBody);
			
			HashMap<String,String> delta = new HashMap<String, String>();
			delta.put("msgBody", msgBody);
			delta.put("subject", subject);
			delta.put("from", from);
			delta.put("toAll", toAll);
			delta.put("to", to);
			delta.put("mode", "READ");
			
			utils.putToState(delta);
			
			loadingPanel.setVisible(false);
			mainMailPanel.setVisible(true);
			init();
		}
		
		@Override
		public void onFailure(Throwable caught) {
			Log.error("loadContacts userId: " + utils.retrUserId(), caught);
			failuresCount++;
			if(failuresCount < 2){
				loadContacts();
			}else{
				utils.alert(caught.getMessage());
				utils.putToPrivateSate("contacts#" + utils.retrUserId(), "");
				loadingPanel.setVisible(false);
				mainMailPanel.setVisible(true);
				init();
			}
			
		}
	}
	
	
	
	private void loadContacts() {
		final String userId = utils.retrUserId();
		try {
			Log.info("Loading contacts for: " + userId);
			utils.showStaticMessage(constants.loadingContacts());
			img0.setVisible(true);
			service.loadContacts(userId, new LoadContactsAsyncCallback());
		} catch (RequestException e) {
			Log.error("loadContacts userId: " + userId , e);
		}
	}


	private void init() {
		//init to suggest box
		contentRichTextArea.setStylePrimaryName(resources.globalCSS().whiteRow());
		String isHideContentStr = utils.retrFromPrivateSate("isHideContent");
		if(isHideContentStr == null){
			isHideContentStr = "false";
			utils.putToPrivateSate("isHideContent","false");
		}
		boolean isHideContent = Boolean.parseBoolean(isHideContentStr);
		setUpIsHideContent(isHideContent);
		String mode = utils.retrFromState("mode");
		Log.info("mode is: " + mode);
		
		
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		if(concatinatedContacts == null){
			concatinatedContacts = utils.retrFromPrivateSate("contacts#" + utils.retrUserId());
		}
		if(concatinatedContacts == null){
			concatinatedContacts = utils.retrFromState("contacts#" + utils.retrUserId());
		}
		if(concatinatedContacts != null && !"".equals(concatinatedContacts)){
			 // Define the oracle that finds suggestions
		    String[] words = concatinatedContacts.split("#");
		    for (int i = 0; i < words.length; ++i) {
		      oracle.add(words[i]);
		    }
		    
		    if(oracleHolderCntPnl.getWidgetCount() > 0){
				oracleHolderCntPnl.clear();
			}
			suggestCntBox = new SuggestBox(oracle);
			suggestCntBox.setWidth(constants.basicItemWidthStr());
			suggestCntBox.getTextBox().addChangeHandler(suggestCntChangeHandler);
			suggestCntBox.getTextBox().addClickHandler(suggestClickHandler);
			
			suggestCntBox.setStyleName(resources.globalCSS().readonly());
			suggestCntBox.setText("Start typing contact address to activate auto-complete.");
			
			oracleHolderCntPnl.add(suggestCntBox);
		}else{
			TextBox dummyTextBox = new TextBox();
			dummyTextBox.setWidth(constants.basicItemWidthStr());
			dummyTextBox.setEnabled(false);
			dummyTextBox.setText(constants.needImportContacts());
		    if(oracleHolderCntPnl.getWidgetCount() > 0){
				oracleHolderCntPnl.clear();
			}
			oracleHolderCntPnl.add(dummyTextBox);
		}
		
		importContactsBtn.setText(constants.importCnts());
		
		
		if( "READ".equals(mode)){//email received from some sender
			readFromStateIntoGadget();
			enableDisableForm(false);
			btn1.setText(constants.replyStr());
			btn2.setText(constants.forwardStr());
			btn2.setVisible(true);
			activitiesMap.put("activity", "done/reply");
			if(isFirstInit){
				utils.reportPageview("/wavyemail/READ");
			}
			
		}else if (mode == null || "".equals(mode) || "NEW".equals(mode)){//gadget just inserted - need to compose and send email
			enableDisableForm(true);
			clearFields();
			readFromStateIntoGadget();
			disableField(fromBox);
			btn1.setText(constants.sendStr());
			String fromStr = constructFullMailAddress();
			fromBox.setText(fromStr);
			activitiesMap.put("activity", "edit/new");
			if(isFirstInit){
				utils.reportPageview("/wavyemail/NEW");
			}
		}else if("SENT".equals(mode)){//email was composed and sent   forwardStr
			readFromStateIntoGadget();
			enableDisableForm(false);
			btn1.setText(constants.forwardStr());
			activitiesMap.put("activity", "done/send");
		}
		contentRichTextArea.setStylePrimaryName(resources.globalCSS().whiteRow());
		isFirstInit = false;
	}
	
	protected ChangeHandler suggestCntChangeHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent event) {
			Timer t = new Timer() {
				@Override
				public void run() {
					String suggestion = suggestCntBox.getTextBox().getText();
					String txt = toBox.getText();
					if(suggestion.length() < 4 || !suggestion.contains("@") ||  txt.contains(suggestion)){
						suggestCntBox.getTextBox().setText("");
						return;
					}
					if(suggestion != null && suggestion.length() > 0){
						suggestCntBox.getTextBox().setText("");
						toBox.setText(suggestion + ", " + toBox.getText());
					}
				}
			};
			t.schedule(200);
		}
	};
	
	protected ClickHandler suggestClickHandler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			suggestCntBox.removeStyleName(resources.globalCSS().readonly());
			suggestCntBox.setText("");
			utils.reportEvent("/suggestContact/","suggestAction", utils.retrUserId(), 1);
		}
	};


	private void readFromStateIntoGadget() {
		String msgBody = utils.retrFromState("msgBody");
		Log.info("msgBody from state: " + msgBody);
		
		if(msgBody != null){
			msgBody = decode(msgBody);
			contentRichTextArea.setHTML(msgBody);
		}
		
		String to = utils.retrFromState("toAll");
		if(to != null){
			to = decode(to);
			toBox.setText(to);
		}
		
		String from = utils.retrFromState("from");
		if(from != null){
			from = decode(from);
			fromBox.setText(from);
		}else{
			from = constructFullMailAddress();
		}
		
		String subject = utils.retrFromState("subject");
		subject = decode(subject);
		if(subject != null){
			subjectBox.setText(subject);
		}
		contentRichTextArea.setStylePrimaryName(resources.globalCSS().whiteRow());
	}
	
	private void writeToStateIntoGadget() {
		String msgBody = contentRichTextArea.getHTML();
		HashMap<String,String> delta = new HashMap<String, String>();
		
		if(msgBody != null){
			msgBody = encode(msgBody);
			delta.put("msgBody", msgBody);
		}
		
		String to = toBox.getText();
		if(to != null){
			to = encode(to);
			delta.put("toAll",to);
		}
		
		String from = fromBox.getText();
		if(from != null){
			from = encode(from);
			delta.put("from",from);
		}
		
		String subject = subjectBox.getText();
		if(subject != null){
			subject = encode(subject);
			delta.put("subject",subject);
		}
		utils.putToState(delta);
	}


	
	
	KeyPressHandler disableKeyPressHandler = new KeyPressHandler() {
		
		public void onKeyPress(KeyPressEvent event) {
			char c = event.getCharCode();
			if(c != '\t'){
				event.preventDefault();
			}
		}
	};

	HashMap<Integer,HandlerRegistration> hadnlersMap = new HashMap<Integer, HandlerRegistration>();
	
	private void disableField( FocusWidget widget) {
		widget.setStyleName(resources.globalCSS().readonly());
//		HandlerRegistration hr = widget.addKeyPressHandler(disableKeyPressHandler);
//		hadnlersMap.put(widget.hashCode(), hr);
	}
	
	private void disableField( SuggestBox widget) {
//		widget.setStyleName(resources.globalCSS().readonly());
//		HandlerRegistration hr = widget.addKeyPressHandler(disableKeyPressHandler);
//		hadnlersMap.put(widget.hashCode(), hr);
	}
	
	private void enableField(FocusWidget widget){
		widget.removeStyleName(resources.globalCSS().readonly());
		HandlerRegistration hr = hadnlersMap.get(widget.hashCode());
		if(hr != null){
			hr.removeHandler();
			hadnlersMap.put(widget.hashCode(), null);
		}
	}
	private void enableField(SuggestBox widget){
		widget.removeStyleName(resources.globalCSS().readonly());
		HandlerRegistration hr = hadnlersMap.get(widget.hashCode());
		if(hr != null){
			hr.removeHandler();
			hadnlersMap.put(widget.hashCode(), null);
		}
	}

	private void enableDisableForm(boolean isEnabled) {
		if(!isEnabled){
			disableField(contentRichTextArea);
			disableField(toBox);
			disableField(fromBox);
			disableField(subjectBox);
		}else{
			enableField(contentRichTextArea);
			enableField(toBox);
			enableField(fromBox);
			enableField(subjectBox);
		}
	}
	
	private void clearFields() {
		contentRichTextArea.setHTML("");
		toBox.setText("");
		fromBox.setText("");
		subjectBox.setText("");
	}
	
	private String constructUserMailAddress() {
		String userId = utils.retrUserId();
		String appDomaniId = utils.retrFromPrivateSate("appDomainId");
		if(appDomaniId == null){
			appDomaniId = "wavyemailbeta";
		}
		String fromStr = ClientMailUtils.waveId2mailId(userId,appDomaniId);
		return fromStr;
	}
	
	private String constructFullMailAddress(){
		String userName = utils.retrUserName();
		return userName + "<" + constructUserMailAddress() + ">";
	}

	private void cancelAction() {
		//contentRichTextArea.setHTML(bodyHtmlBackup);
		btn2.setVisible(false);
		if(btn1.getText().equals(constants.sendStr() )){
			btn1.setText(constants.replyStr());
		}
		init();
		contentRichTextArea.setStylePrimaryName(resources.globalCSS().whiteRow());
		utils.reportEvent("/cancelAction/","editingActions", utils.retrUserId(), 1);
	}
	
	private void initState(){//debug purpose
		String msgBody = "Some message body with hebrew. אבג";
		msgBody = encode(msgBody);
		utils.putToState("msgBody", msgBody );
		utils.putToState("toAll", "Yuri<vega113-googlewave.com@wavyemailbeta.appspotmail.com>" );
		utils.putToState("from", "Yuri Z<vega113@gmail.com>");
		String subject = "Subject for email";
		subject = encode(subject);
		utils.putToState("subject", subject);
		utils.putToState("mode", "READ");
		utils.putToState("contacts#vega113@googlewave.com", "vega113@gmail.com#Yuri Z<vega113@gmail.com>#Yuri<vega113-googlewave.com@wavyemailbeta.appspotmail.com>#vega113-googlewave.com@wavyemailbeta.appspotmail.com#");
//		utils.putToState("personId", "9");
//		utils.putToState("emailEventId", "1");
		
	}


	private void forwardBtnClicked() {
		String subject = subjectBox.getText();
		fromBox.setText(constructFullMailAddress());
		toBox.setText("");
		//add Fw: to subject
		boolean isForwardStrAlreadyAdded =  (subject.indexOf(constants.forwardPrefixStr()) > -1 || subject.indexOf(constants.forwardPrefixStr()) > -1);
		String fixedSubject = isForwardStrAlreadyAdded ? subject : constants.forwardPrefixStr() + subject;
		subjectBox.setText(fixedSubject);
		
		//enable fields besides from
		enableDisableForm(true);
		disableField(fromBox);
		//change button text to send
		btn1.setText(constants.sendStr());
		//enable cancel button
		btn2.setVisible(true);
		
		//insert horizontal rule
		bodyHtmlBackup = contentRichTextArea.getHTML();
		String html1 = contentRichTextArea.getHTML();
		contentRichTextArea.setHTML(html1);
		
		btn2.setVisible(true);
		btn2.setText(constants.cancelStr());
		
		activitiesMap.put("activity", "edit/forward");
		utils.reportEvent("/forwardBtnClicked/","editingActions", utils.retrUserId(), 1);
	}


	private void replyBtnClicked() {
		//switch between from and to
		String from = fromBox.getText();
		String subject = subjectBox.getText();
		fromBox.setText(constructFullMailAddress());
		toBox.setText(from);
		//add RE: to subject
		boolean isReplyStrAlreadyAdded =  (subject.indexOf(constants.replyPrefixStr()) > -1 || subject.indexOf(constants.replyCapPrefixStr()) > -1);
		String fixedSubject = isReplyStrAlreadyAdded ? subject : constants.replyPrefixStr() + subject;
		subjectBox.setText(fixedSubject);
		
		//enable fields besides from
		enableDisableForm(true);
		disableField(fromBox);
		//change button text to send
		btn1.setText(constants.sendStr());
		//enable cancel button
		btn2.setVisible(true);
		
		//insert horizontal rule
		bodyHtmlBackup = contentRichTextArea.getHTML();
		contentRichTextArea.getFormatter().insertHorizontalRule();
		String html1 = contentRichTextArea.getHTML();
		contentRichTextArea.setHTML("<br><br>" + html1);
		
		activitiesMap.put("activity", "edit/reply");
		
		btn2.setVisible(true);
		btn2.setText(constants.cancelStr());
		utils.reportEvent("/replyBtnClicked/","editingActions", utils.retrUserId(), 1);
	}


	private String encode(String text) {
		text = UnicodeString.convert(text);
		return text;
	}
	
	private String decode(String text) {
		text = UnicodeString.deconvert(text);
		return text;
	}


	private void setUpIsHideContent(boolean isHideContent) {
		
		if( isHideContent ){
			//unhide
			contentRichTextAreaCptnPnl.setVisible(false);
			hideContentAnchor.setText(constants.unhideContentStr());
			
		}else{
			contentRichTextAreaCptnPnl.setVisible(true);
			hideContentAnchor.setText(constants.hideContentStr());
		}
	}
	
	

}
