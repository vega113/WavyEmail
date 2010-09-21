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
import com.google.gwt.event.shared.DefaultHandlerRegistration;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
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
	@UiField Button btn1;
	@UiField Button btn2;
	@UiField Button btn3;
	@UiField Image img0;
	@UiField SimplePanel richTextToolbarPanel;
	@UiField VerticalPanel loadingPanel;
	@UiField VerticalPanel mainMailPanel;
	
	SuggestBox suggestCntBox;
	
	@UiField
	Button registerBtn;
	
	
	
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
		
		
//		utils.putToState("mode","NEW");
		
		
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
						
					
					String subject = subjectBox.getText();
					
					subject = UnicodeString.convert(subject);
					subject = ClientMailUtils.encodeBase64(subject);
					subjectBackup = subject;
					
					String msgBody = contentRichTextArea.getHTML();
					bodyHtmlBackup = msgBody;
					
					msgBody = UnicodeString.convert(msgBody);
					msgBody = ClientMailUtils.encodeBase64(msgBody);

					final String sender = constructUserMailAddress();
					final String senderName = utils.retrUserName();
					Log.info("Sending Subject: " + subjectBackup + ", MsgBody: " + bodyHtmlBackup);
					String waveId = utils.retrWaveId();
					String iconUrl = utils.retrUserThumbnailUrl();
					String blipId = utils.retrFromState("blipId");
					if(blipId == null){
						blipId = "none";
					}
					try {
						img0.setVisible(true);
						service.sendEmail(recipients,subject,msgBody,sender,senderName,activityType,waveId,blipId,iconUrl, new AsyncCallback<JSONValue>() {

							@Override
							public void onSuccess(JSONValue result) {
								Log.info("Entering  onSuccess: " + result.toString());
								img0.setVisible(false);
								String activity = activitiesMap.get("activity");
								String msgBodyForState = null;
								if(activity.contains("reply")){
									msgBodyForState = "<span style=\"font-size: 8pt; color:#B8B8B8\"><i>" + messages.infoReplySent(new Date(),recipients) +"</i></span><br>" + bodyHtmlBackup;
								}else if(activity.contains("new")){
									msgBodyForState = bodyHtmlBackup;
								}else if(activity.contains("forward")){
									msgBodyForState = "<span style=\"font-size: 8pt; color:#B8B8B8\"><i>" + messages.infoForwardSent(new Date(),recipients) +"</i></span><br>" + bodyHtmlBackup;
								}
								Log.info("Activity is: " + activity);
								try{
									Log.info("msgBodyForState before encode: " + msgBodyForState);
									
									msgBodyForState = UnicodeString.convert(msgBodyForState);
									msgBodyForState = ClientMailUtils.encodeBase64(msgBodyForState);
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
								}catch(Exception e){
									Log.error(msgBodyForState, e);
								}
//								cancelReply(); //XXX remove after verification 
								//robot should insert as reply new gadget with reply content
							}

							@Override
							public void onFailure(Throwable caught) {
								utils.alert(caught.getMessage());
								img0.setVisible(false);
							}
						});
					} catch (RequestException e) {
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
		
		init();
		
		Timer mailPnlTimer = new Timer() {
			@Override
			public void run() {
				loadContacts();
//				if(utils.retrFromState("contacts") == null){
//					loadContacts();
//				}
				loadingPanel.setVisible(false);
				mainMailPanel.setVisible(true);
			}
		};
		mailPnlTimer.schedule(1000);//TODO ?should send request to server - to get info for the user
		
		//run on state update - after sent/reply
		utils.addRunOnStateEventUpdate(new Runnable() {
			
			@Override
			public void run() {
				Log.info("addRunOnStateEventUpdate");
				cancelAction();//restore gadget from state
			}
		});
		
		
		registerBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
//				Window.open("http://" + constants.appDomain() + ".appspot.com/LoginServlet?user=" + utils.retrHostId(), "", "");
				Window.open("http://localhost:8888/LoginServlet?user=" + utils.retrHostId(), "", "");
				utils.putToState("contacts",null);
			}
		});
	}
	

	private void loadContacts() {
		final String userId = utils.retrUserId();
		try {
			Log.info("Loading contacts for: " + userId);
			service.loadContacts(userId, new AsyncCallback<JSONValue>() {
				
				@Override
				public void onSuccess(JSONValue result) {
					String contacts = result.isObject().get("contacts").isString().stringValue();
					utils.putToState("contacts", contacts);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Log.error("loadContacts userId: " + userId , caught);
					utils.alert(caught.getMessage());
					img0.setVisible(false);
				}
			});
		} catch (RequestException e) {
			Log.error("loadContacts userId: " + userId , e);
		}
	}


	private void init() {
		//init to suggest box
		String mode = utils.retrFromState("mode");
		Log.info("mode is: " + mode);
		
		String concatinatedContacts = utils.retrFromState("contacts");
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		if(concatinatedContacts != null){
			 // Define the oracle that finds suggestions
		    String[] words = concatinatedContacts.split("#");
		    for (int i = 0; i < words.length; ++i) {
		      oracle.add(words[i]);
		    }
		}
		
		if(oracleHolderCntPnl.getWidgetCount() > 0){
			oracleHolderCntPnl.clear();
		}
		suggestCntBox = new SuggestBox(oracle);
		suggestCntBox.setWidth(constants.basicItemWidthStr());
		suggestCntBox.getTextBox().addChangeHandler(suggestCntChangeHandler);
		oracleHolderCntPnl.add(suggestCntBox);
		
		if( "READ".equals(mode)){//email received from some sender
			readFromStateIntoGadget();
			enableDisableForm(false);
			btn1.setText(constants.replyStr());
			btn2.setText(constants.forwardStr());
			btn2.setVisible(true);
			activitiesMap.put("activity", "done/reply");
			
		}else if (mode == null || "".equals(mode) || "NEW".equals(mode)){//gadget just inserted - need to compose and send email
			enableDisableForm(true);
			clearFields();
			
			disableField(fromBox);
			btn1.setText(constants.sendStr());
			String fromStr = constructFullMailAddress();
			fromBox.setText(fromStr);
			activitiesMap.put("activity", "edit/new");
		}else if("SENT".equals(mode)){//email was composed and sent   forwardStr
			readFromStateIntoGadget();
			enableDisableForm(false);
			btn1.setText(constants.forwardStr());
			activitiesMap.put("activity", "done/send");
		}
		
	}
	
	protected ChangeHandler suggestCntChangeHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent event) {
			Timer t = new Timer() {
				@Override
				public void run() {
					String suggestion = suggestCntBox.getTextBox().getText();
					if(toBox.getText().contains(suggestion)){
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


	private void readFromStateIntoGadget() {
		String msgBody = utils.retrFromState("msgBody");
		Log.info("encoded msgBody: " + msgBody);
		try{
			msgBody = ClientMailUtils.decodeBase64(msgBody);
			msgBody = UnicodeString.deconvert(msgBody);
			//XXX deconvert here
		}catch(Exception e){
			Log.error(msgBody, e);
		}
		Log.info("decoded msgBody: " + msgBody);
		if(msgBody != null){
			contentRichTextArea.setHTML(msgBody);
		}
		
		String to = utils.retrFromState("toAll");
		if(to != null){
			toBox.setText(to);
		}
		
		String from = utils.retrFromState("from");
		if(msgBody != null){
			fromBox.setText(from);
		}
		
		String subject = utils.retrFromState("subject");
		subject = ClientMailUtils.decodeBase64(subject);
		subject = UnicodeString.deconvert(subject);
		if(subject != null){
			subjectBox.setText(subject);
		}
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
			appDomaniId = "mailwavybeta";
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
	}
	
	private void initState(){
		String msgBody = "Some message body with hebrew. אבג";
		msgBody = UnicodeString.convert(msgBody);
		msgBody = ClientMailUtils.encodeBase64(msgBody);
		utils.putToState("msgBody", msgBody );
		utils.putToState("toAll", "Yuri<vega113-googlewave.com@mailwavybeta.appspotmail.com>" );
		utils.putToState("from", "Yuri Z<vega113@gmail.com>");
		String subject = "Subject for email";
		subject = UnicodeString.convert(subject);
		subject = ClientMailUtils.encodeBase64(subject);
		utils.putToState("subject", subject);
		utils.putToState("mode", "READ");
		utils.putToState("contacts", "vega113@gmail.com#Yuri Z<vega113@gmail.com>#Yuri<vega113-googlewave.com@mailwavybeta.appspotmail.com>#vega113-googlewave.com@mailwavybeta.appspotmail.com#");
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
	}

}
