/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.callhistory;

import java.util.*;

import net.java.sip.communicator.service.callhistory.*;
import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * The <tt>CallHistorySourceContact</tt> is an implementation of the
 * <tt>SourceContact</tt> interface based on a <tt>CallRecord</tt>.
 *
 * @author Yana Stamcheva
 */
public class CallHistorySourceContact implements SourceContact
{
    /**
     * The parent <tt>CallHistoryContactSource</tt>, where this contact is
     * contained.
     */
    private final CallHistoryContactSource contactSource;

    /**
     * The corresponding call record.
     */
    private final CallRecord callRecord;

    /**
     * The incoming call icon.
     */
    private static final byte[] incomingIcon
        = CallHistoryActivator.getResources()
            .getImageInBytes("service.gui.icons.INCOMING_CALL");

    /**
     * The outgoing call icon.
     */
    private static byte[] outgoingIcon
        = CallHistoryActivator.getResources()
            .getImageInBytes("service.gui.icons.OUTGOING_CALL");

    /**
     * The missed call icon.
     */
    private static byte[] missedCallIcon
        = CallHistoryActivator.getResources()
            .getImageInBytes("service.gui.icons.MISSED_CALL");

    /**
     * A list of all contact details.
     */
    private final List<ContactDetail> contactDetails
        = new LinkedList<ContactDetail>();

    /**
     * The display name of this contact.
     */
    private String displayName = "";

    /**
     * The display details of this contact.
     */
    private final String displayDetails;

    /**
     * Creates an instance of <tt>CallHistorySourceContact</tt> 
     * @param contactSource
     * @param callRecord
     */
    public CallHistorySourceContact(CallHistoryContactSource contactSource,
                                    CallRecord callRecord)
    {
        this.contactSource = contactSource;
        this.callRecord = callRecord;

        this.initPeerDetails();

        this.displayDetails
            = CallHistoryActivator.getResources()
                .getI18NString("service.gui.AT") + ": "
            + getDateString(callRecord.getStartTime().getTime())
            + " " + CallHistoryActivator.getResources()
                .getI18NString("service.gui.DURATION") + ": "
            + GuiUtils.formatTime(
                GuiUtils.substractDates(
            callRecord.getEndTime(), callRecord.getStartTime()));
    }

    /**
     * Initializes peer details.
     */
    private void initPeerDetails()
    {
        Iterator<CallPeerRecord> recordsIter
            = callRecord.getPeerRecords().iterator();

        while (recordsIter.hasNext())
        {
            String peerAddress = recordsIter.next().getPeerAddress();

            if (displayName.length() > 0)
                displayName += "," + peerAddress;
            else
                displayName += peerAddress;

            if (peerAddress != null)
            {
                ContactDetail contactDetail = new ContactDetail(peerAddress);

                Map<Class<? extends OperationSet>, ProtocolProviderService>
                    preferredProviders = null;
                if (callRecord.getProtocolProvider() != null)
                {
                    preferredProviders
                        = new Hashtable<Class<? extends OperationSet>,
                                        ProtocolProviderService>();

                    preferredProviders.put( OperationSetBasicTelephony.class,
                                            callRecord.getProtocolProvider());

                    contactDetail
                        .setPreferredProviders(preferredProviders);
                }

                // Set supported operation sets.
                LinkedList<Class<? extends OperationSet>> supportedOpSets
                    = new LinkedList<Class<? extends OperationSet>>();

                supportedOpSets.add(OperationSetBasicTelephony.class);
                contactDetail.setSupportedOpSets(supportedOpSets);

                contactDetails.add(contactDetail);
            }
        }
    }

    /**
     * Returns a list of available contact details.
     * @return a list of available contact details
     */
    public List<ContactDetail> getContactDetails()
    {
        return new LinkedList<ContactDetail>(contactDetails);
    }

    /**
     * Returns the parent <tt>ContactSourceService</tt> from which this contact
     * came from.
     * @return the parent <tt>ContactSourceService</tt> from which this contact
     * came from
     */
    public ContactSourceService getContactSource()
    {
        return contactSource;
    }

    /**
     * Returns the display details of this search contact. This could be any
     * important information that should be shown to the user.
     *
     * @return the display details of the search contact
     */
    public String getDisplayDetails()
    {
        return displayDetails;
    }

    /**
     * Returns the display name of this search contact. This is a user-friendly
     * name that could be shown in the user interface.
     *
     * @return the display name of this search contact
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * An image (or avatar) corresponding to this search contact. If such is
     * not available this method will return null.
     *
     * @return the byte array of the image or null if no image is available
     */
    public byte[] getImage()
    {
        if (callRecord.getDirection().equals(CallRecord.IN))
        {
            if (callRecord.getStartTime().equals(callRecord.getEndTime()))
                return missedCallIcon;
            else
                return incomingIcon;
        }
        else if (callRecord.getDirection().equals(CallRecord.OUT))
            return outgoingIcon;

        return null;
    }

    /**
     * Returns a list of all <tt>ContactDetail</tt>s supporting the given
     * <tt>OperationSet</tt> class.
     * @param operationSet the <tt>OperationSet</tt> class we're looking for
     * @return a list of all <tt>ContactDetail</tt>s supporting the given
     * <tt>OperationSet</tt> class
     */
    public List<ContactDetail> getContactDetails(
                                    Class<? extends OperationSet> operationSet)
    {
        // We support only call details.
        if (!operationSet.equals(OperationSetBasicTelephony.class))
            return null;

        return new LinkedList<ContactDetail>(contactDetails);
    }

    /**
     * Returns the preferred <tt>ContactDetail</tt> for a given
     * <tt>OperationSet</tt> class.
     * @param operationSet the <tt>OperationSet</tt> class, for which we would
     * like to obtain a <tt>ContactDetail</tt>
     * @return the preferred <tt>ContactDetail</tt> for a given
     * <tt>OperationSet</tt> class
     */
    public ContactDetail getPreferredContactDetail(
        Class<? extends OperationSet> operationSet)
    {
        // We support only call details.
        if (!operationSet.equals(OperationSetBasicTelephony.class))
            return null;

        return contactDetails.get(0);
    }

    /**
     * Returns the date string to show for the given date.
     * 
     * @param date the date to format
     * @return the date string to show for the given date
     */
    public static String getDateString(long date)
    {
        String time = GuiUtils.formatTime(date);

        // If the current date we don't go in there and we'll return just the
        // time.
        if (GuiUtils.compareDatesOnly(date, System.currentTimeMillis()) < 0)
        {
            StringBuffer dateStrBuf = new StringBuffer();

            GuiUtils.formatDate(date, dateStrBuf);
            dateStrBuf.append(" ");
            dateStrBuf.append(time);
            return dateStrBuf.toString();
        }

        return time;
    }
}