/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.muc;

import java.util.*;

import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.muc.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * Source contact for the chat rooms.
 * 
 * @author Hristo Terezov
 */
public class ChatRoomSourceContact
    extends SortedGenericSourceContact
{
    /**
     * The name of the chat room associated with the contact.
     */
    private String chatRoomName;
    
    /**
     * The ID of the chat room associated with the contact.
     */
    private String chatRoomID;
    
    /**
     * The protocol provider of the chat room associated with the contact.
     */
    private ProtocolProviderService provider;
    
    /**
     * 
     * @param chatRoomName
     * @param chatRoomID
     * @param query
     * @param pps
     */
    public ChatRoomSourceContact(String chatRoomName, 
        String chatRoomID, ChatRoomQuery query, ProtocolProviderService pps)
    {
        super(query, query.getContactSource(), chatRoomName,
            generateDefaultContactDetails(chatRoomName));
        
        this.chatRoomName = chatRoomName;
        this.chatRoomID = chatRoomID;
        this.provider = pps;
        
        initContactProperties(getChatRoomStateByName());
        
    }
    
    /**
     * 
     * @param chatRoom
     * @param query
     */
    public ChatRoomSourceContact(ChatRoom chatRoom, ChatRoomQuery query)
    { 
        super(query, query.getContactSource(), chatRoom.getName(),
            generateDefaultContactDetails(chatRoom.getName()));
        
        this.chatRoomName = chatRoom.getName();
        this.chatRoomID = chatRoom.getIdentifier();
        this.provider = chatRoom.getParentProvider();
        
        initContactProperties(
            (chatRoom.isJoined()? 
                ChatRoomPresenceStatus.CHAT_ROOM_ONLINE : 
                    ChatRoomPresenceStatus.CHAT_ROOM_OFFLINE));
        
    }
    
    /**
     * Sets the given presence status and the name of the chat room associated with the 
     * contact.
     * @param status the presence status to be set.
     */
    private void initContactProperties(PresenceStatus status)
    {
        setPresenceStatus(status);
        setContactAddress(chatRoomName);
    }
    
    /**
     * Checks if the chat room associated with the contact is joinned or not and 
     * returns it presence status.
     * 
     * @return the presence status of the chat room associated with the contact.
     */
    private PresenceStatus getChatRoomStateByName()
    {
        for(ChatRoom room : 
                provider.getOperationSet(OperationSetMultiUserChat.class)
                    .getCurrentlyJoinedChatRooms())
        {
            if(room.getName().equals(chatRoomName))
            {
                return ChatRoomPresenceStatus.CHAT_ROOM_ONLINE;
            }
        }
        return ChatRoomPresenceStatus.CHAT_ROOM_OFFLINE;
    }
    
    /**
     * Generates the default contact details for <tt>ChatRoomSourceContact</tt>
     * instances.
     * 
     * @param chatRoomName the name of the chat room associated with the contact
     * @return list of default <tt>ContactDetail</tt>s for the contact. 
     */
    private static List<ContactDetail> generateDefaultContactDetails(
        String chatRoomName)
    {
        ContactDetail contactDetail
            = new ContactDetail(chatRoomName);
        List<Class<? extends OperationSet>> supportedOpSets
            = new ArrayList<Class<? extends OperationSet>>();
    
        supportedOpSets.add(OperationSetMultiUserChat.class);
        contactDetail.setSupportedOpSets(supportedOpSets);
    
        List<ContactDetail> contactDetails
            = new ArrayList<ContactDetail>();
    
        contactDetails.add(contactDetail);
        return contactDetails;
    }
    
    /**
     * Returns the id of the chat room associated with the contact.
     * 
     * @return the chat room id.
     */
    public String getChatRoomID()
    {
        return chatRoomID;
    }

    /**
     * Returns the name of the chat room associated with the contact.
     * 
     * @return the chat room name
     */
    public String getChatRoomName()
    {
        return chatRoomName;
    }

    /**
     * Returns the provider of the chat room associated with the contact.
     * 
     * @return the provider
     */
    public ProtocolProviderService getProvider()
    {
        return provider;
    }

}
