package de.webfilesys.unix;

public class FileSysInfo
{
    public String mount_point;
    public String dev_name;
    public long capacity;
    public long free;
    public int percent_used;
    public int percent_i_used;

    public FileSysInfo next;

    public FileSysInfo(
        String mount_point,
        String dev_name,
        long capacity,
        long free,
        int percent_used,
        int percent_i_used)
    {
        this.mount_point = new String(mount_point);
        this.dev_name = new String(dev_name);
        this.capacity = capacity;
        this.free = free;
        this.percent_used = percent_used;
        this.percent_i_used = percent_i_used;
        next = null;
    }
}