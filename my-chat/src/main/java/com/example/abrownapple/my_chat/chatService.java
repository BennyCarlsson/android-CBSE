package com.example.abrownapple.my_chat;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.result.Result;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.customobjects.result.QBCustomObjectLimitedResult;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//API http://quickblox.com
//Followed guides and reused code from http://quickblox.com/developers/

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


    //call method to create groupchat
    //in: ArrayList<integer> with id's of people to join, String name of chat
    //out; QBDialog
    public QBDialog createGroupChat(ArrayList<Integer> occupantIdsList, String chatName) {
        QBDialog dialog = new QBDialog();
        dialog.setName(chatName);
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
        return dialog;
    }

    private ArrayList<QBUser> userList = new ArrayList<QBUser>();
    //find a user by their username
    //in string username
    //out ArrayList<QBUser> userList
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

    //find user by user id
    //in: int userId
    //out ArrayList<QBUser> userList
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

    private ArrayList<QBChatMessage> chatMessage;

    //gets chathistory
    //in: QBDialog
    //out: ArrayList<QBChatMessage>
    public ArrayList<QBChatMessage> loadChatHistory(QBDialog dialog){
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");
        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                chatMessage = messages;
            }
            @Override
            public void onError(List<String> errors) {
            }
        });
        return chatMessage;
    }

    //check if user is online
    //in: QBUser
    //out: boolean true = online false = offline
    public boolean checkIfUserOnline(QBUser user){
        long currentTime = System.currentTimeMillis();
        long userLastRequestAtTime = user.getLastRequestAt().getTime();

        // if user didn't do anything last 5 minutes (5*60*1000 milliseconds)
        if((currentTime - userLastRequestAtTime) > 5*60*1000){
            return false;
        }
        return true;
    }

    //send Files
    //in: String file name, QBDialog
    public void sendMessageWithFile(String fileName, QBDialog dialog){
        final QBDialog currentDialog = dialog;
        File file = new File(fileName); //examle "potato.png"
        Boolean fileIsPublic = false;
        QBContent.uploadFileTask(file, fileIsPublic, null, new QBEntityCallbackImpl<QBFile>() {
            @Override
            public void onSuccess(QBFile file, Bundle params) {

                // create a message
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setProperty("save_to_history", "1"); // Save a message to history

                // attach a file
                QBAttachment attachment = new QBAttachment("file");
                attachment.setId(file.getId().toString());
                chatMessage.addAttachment(attachment);

                QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
                QBGroupChat groupChat = groupChatManager.createGroupChat(currentDialog.getRoomJid());


                try {
                    groupChat.sendMessage(chatMessage);
                } catch (XMPPException e) {

                } catch (SmackException.NotConnectedException e) {

                } catch (IllegalStateException e){

                }
            }

            @Override
            public void onError(List<String> errors) {
                // error
            }
        });
    }

    //Call this method to send a message
    //in: QBDialog, String message
    public void sendMessage(QBDialog dialog, String message){
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        QBGroupChat groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(message);
        chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history

        try {
            groupChat.sendMessage(chatMessage);
        } catch (XMPPException e) {

        } catch (SmackException.NotConnectedException e) {

        } catch (IllegalStateException e){

        }
    }
}
