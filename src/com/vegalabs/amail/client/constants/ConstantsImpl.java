

package com.vegalabs.amail.client.constants;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Constants.DefaultIntValue;
import com.google.gwt.i18n.client.Constants.DefaultStringValue;

/**
 * Interface that can be used to read constants from a properties file.
 *
 * Instances of this interface can be requested/injected by Gin without an
 * explicit binding: Gin will internallt call GWT.create on the requested type.
 */
public interface ConstantsImpl extends Constants {

	String doneStr();
	String loadingContacts();
	String refreshContacts();
	String emailSentStr();
	String sendingEmailStr();
	String contentSaveSuccess();
	String installerLinkStr();
	String mailForumLinkStr();
	String saveContentStr();
	String needImportContacts();
	String unhideContentStr();
	String hideContentStr();
	String importCnts();
	String lookupCntsTitle();
	String addBtnStr();
	String fromSndrStr();
	String toRcpntStr();
	String lookupCntsStr();
	String forwardPrefixStr();
	String forwardStr();
	String cancelStr();
	String replyCapPrefixStr();
	String replyPrefixStr();
	String sendStr();
	String replyStr();
	String noRecipientWarningStr();
	String loadingStr();
	
	
	
	
	@DefaultStringValue(value="wavyemailbeta")
	String appDomain();
	//urls
	@DefaultStringValue(value="#restored:wave:googlewave.com%252Fw%252B1OG6ZLZkByJ")
	String discussWavyEmailUrl();
	@DefaultStringValue(value="#restored:wave:googlewave.com%252Fw%252B-va3a8I6C")
	String installWavyEmailUrl();
	
	@DefaultStringValue(value="UA-13269470-3")
	String trackerUIStr();
	//size
	/** width of the whole gadget*/
	@DefaultStringValue(value="760px")//change both
	String basicWidthStr();
	@DefaultIntValue(value=510)//change both
	int basicWidthInt();
	
	/**  width of scroll panel */
	@DefaultStringValue(value="734px")
	String smallerWidthStr();
	/** widht of create panel*/
	@DefaultStringValue(value="734px")
	String smallerBiggerWidthStr();
	
	@DefaultIntValue(value=320)
	int basicReportHeightInt();
	@DefaultStringValue(value="72px")
	String basicItemScrollHeightStr();
	/**width of AddRemDefLabel*/
	@DefaultStringValue(value="644px")
	String basicItemWidthStr();
	/** width of input text boxes on admin tab*/
	@DefaultStringValue(value="164px")
	String basicTextBoxWidthStr();
	@DefaultIntValue(value=25)
	int itemCreateHeight();
	@DefaultStringValue(value="238px")
	String aboutTabScrollHeight();
	
	@DefaultStringValue(value="UA-13269470-4")
	String ANALYTICS_ID();
	
	//-------------------------------------
	String newWavesLast14Days();
	String breakDown4AllTags();
	String reportTabStr();
	String noForumSelectedWarning();
	String selectReportTypeStr();
	String selectDigestStr();
	String newBlipsLast14Days();
	String activeContributors14Days();
	String influenceContributors14Days();
	
	
	
	
	

}
