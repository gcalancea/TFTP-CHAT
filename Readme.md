# TFTP Chat Room

### Project description:

Several chat clients will be able to chat between themselves and send files to 
one another through the chat server, which communicates with a TFTP client and a
TFTP server.


### Main components:
 
- ***TFTP Server*** - handles the request by using the TFTPConnectionHandler (It can receive WRITE_REQ,
 READ_REQ, depending on the type of operation: upload, download)

- ***TFTP Client*** - the middleware between the chat server (MyServer class) and the TFTPServer;
 from the chat client request (MyClient), it goes to the MyServer and then it is sent to the
 TFTP Client, where the request to the TFTPServer is built as a datagram and sent through UDP
 
- ***MyServer*** - the chat server, which handles the connections with clients; it is a multi-threaded
server. When a client connects, it creates a new thread to communicate with him

- ***MyClient*** - has a thread for sending messages to the server and a thread for receiving them;

- ***ProgressBarTest*** - a Java Swing Interface which is directly connected to the TFTP Server and
shows the current progress for upload & download and the available files on server


### Main functionality and commands:

- *send messages* between clients as a group chat

- *upload* a file on the server

        upload <path_to_file>

- *download* a file from the server
        
        download <filename> <path_to_save_folder_filename>

- *list* all current files available on the server
    
        list

- receive *help* from a "BOT" about the possible commands
    
        help

- use the keyword *"bye"* to close the connection

        bye
        
        
        
### How to start the application

 - compile the project
 
 - run ***TFTPServer***
 
 - run ***MyServer***
 
 - run ***MyClient*** as many times as you want
 
 