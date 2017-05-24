package tftp_server;


public class FileItem {
    private String name;

    public FileItem(String fl_name){
        name = fl_name;
    }

    public String getName(){
        return this.name;
    }
}
