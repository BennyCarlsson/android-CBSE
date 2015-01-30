package com.example.abrownapple.my_chat;
import android.os.Bundle;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import java.util.ArrayList;
import java.util.List;

public class chatService {
    QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();
    QBMessageListener<QBPrivateChat> privateChatMessageListener = new QBMessageListener<QBPrivateChat>() {
        @Override
        public void processMessage(QBPrivateChat privateChat, final QBChatMessage chatMessage) {

        }
        @Override
        public void processError(QBPrivateChat privateChat, QBChatException error, QBChatMessage originMessage){

        }
        @Override
        public void processMessageDelivered(QBPrivateChat privateChat, String messageID){

        }
        @Override
        public void processMessageRead(QBPrivateChat privateChat, String messageID){
        }
    };

    public void sendMessage(){
        QBPrivateChat privateChat = privateChatManager.getChat(35);
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(35, privateChatMessageListener);
        }

        try {
            privateChat.sendMessage("Hi there!");
        } catch (XMPPException e) {

        } catch (SmackException.NotConnectedException e) {

        }
    }

    public void groupChat() {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        occupantIdsList.add(34);
        occupantIdsList.add(17);

        QBDialog dialog = new QBDialog();
        dialog.setName("Chat with Garry and John");
        dialog.setPhoto("1786");
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        groupChatManager.createDialog(dialog, new QBEntityCallbackImpl<QBDialog>() {
            @Override
            public void onSuccess(QBDialog dialog, Bundle args) {

            }

            @Override
            public void onError(List<String> errors) {

            }
        });
    }

    private ArrayList<QBUser> userList = new ArrayList<QBUser>();
    public ArrayList<QBUser> getUserByName(String Username){
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsersByFullName(Username, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                userList = users;
            }
            @Override
            public void onError(List<String> errors) {

            }
        });
        return userList;
    }
    public ArrayList<QBUser> getUserById(int userId){
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        ArrayList<Integer> usersIds = new ArrayList<Integer>();
        usersIds.add(userId);

        QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    userList = users;
            }

            @Override
            public void onError(List<String> errors) {

            }
        });
        return userList;
    }
    //Todo getContactList
    public List<String> getFriendList(){
        List <String> list = new ArrayList<String>();
        return list;
    }
    public void addContact(QBUser user){
        //todo addContact
    }
    public void removeContact(QBUser user){
        //todo remove contact
    }
    //Todo
    //fixa grupp / private chat
    //hämta chat meddelande
    //se vilka som är online
    //
}
