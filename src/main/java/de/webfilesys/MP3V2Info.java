package de.webfilesys;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v2_2;
import org.farng.mp3.id3.ID3v2_2Frame;

public class MP3V2Info
{
    private static String[] genreTable=
    {
        "Blues",
        "Classic Rock",
        "Country",
        "Dance",
        "Disco",
        "Funk",
        "Grunge",
        "Hip-Hop",
        "Jazz",
        "Metal",
        "New Age",
        "Oldies",
        "Other",
        "Pop",
        "R&B",
        "Rap",
        "Reggae",
        "Rock",
        "Techno",
        "Industrial",
        "Alternative",
        "Ska",
        "Death Metal",
        "Pranks",
        "Soundtrack",
        "Euro-Techno",
        "Ambient",
        "Trip-Hop",
        "Vocal",
        "Jazz+Funk",
        "Fusion",
        "Trance",
        "Classical",
        "Instrumental",
        "Acid",
        "House",
        "Game",
        "Sound Clip",
        "Gospel",
        "Noise",
        "AlternRock",
        "Bass",
        "Soul",
        "Punk",
        "Space",
        "Meditative",
        "Instrumental Pop",
        "Instrumental Rock",
        "Ethnic",
        "Gothic",
        "Darkwave",
        "Techno-Industrial",
        "Electronic",
        "Pop-Folk",
        "Eurodance",
        "Dream",
        "Southern Rock",
        "Comedy",
        "Cult",
        "Gangsta",
        "Top 40",
        "Christian Rap",
        "Pop/Funk",
        "Jungle",
        "Native American",
        "Cabaret",
        "New Wave",
        "Psychadelic",
        "Rave",
        "Showtunes",
        "Trailer",
        "Lo-Fi",
        "Tribal",
        "Acid Punk",
        "Acid Jazz",
        "Polka",
        "Retro",
        "Musical",
        "Rock & Roll",
        "Hard Rock",
        "Unknown"
    };

    private String title=null;
    private String artist=null;
    private String album=null;
    private String publishYear=null;
    private String comment=null;
    private int genreCode = (-1);
    private String genreText = null;
    
    private boolean containsPicture = false;
    
    String fileName=null;

    boolean tagIncluded=false;

    public MP3V2Info(String path)
    {
        tagIncluded=false;

        fileName=path;

        File tmpFile = new File(path);

        if ((!tmpFile.exists()) || (!tmpFile.isFile()))
        {
            Logger.getLogger(getClass()).error("MP3 file not found: " + path);
            return;
        }

        if (!tmpFile.canRead())
        {
            Logger.getLogger(getClass()).error("MP3 file is not readable: " + path);
            return;
        }

        try
        {
            MP3File mp3File = new MP3File(tmpFile);
            
            if (mp3File.hasID3v1Tag())
            {
                ID3v1 id3v1 = mp3File.getID3v1Tag();            

                tagIncluded = true;
                
                title = id3v1.getTitle();

                artist = id3v1.getArtist();

                album = id3v1.getAlbum();

                publishYear = id3v1.getYear();

                comment = id3v1.getComment();
            
                genreCode = id3v1.getGenre();
            }

            if (mp3File.hasID3v2Tag())
            {
                AbstractID3v2 id3v2 = mp3File.getID3v2Tag();

                if (!tagIncluded) 
                {
                    tagIncluded = true;

                    title = id3v2.getSongTitle();
                    artist = id3v2.getLeadArtist();
                    album = id3v2.getAlbumTitle();
                    publishYear = id3v2.getYearReleased();
                    comment = id3v2.getSongComment();
                    genreText = id3v2.getSongGenre();
                }
                
                // lyrics = id3v2_2.getSongLyric();
                
                Iterator iter = id3v2.getFrameIterator();
                
                while ((!containsPicture) && iter.hasNext())
                {
                    Object o = iter.next();
                    if (o instanceof ID3v2_2Frame)
                    {
                        ID3v2_2Frame frame = (ID3v2_2Frame) o;
                        
                        if (frame.getIdentifier().startsWith("APIC"))
                        {
                            containsPicture = true;
                        }
                    }
                }
            }
        }
        catch (IOException nfex)
        {
            Logger.getLogger(getClass()).error("cannot read MP3 file: " + nfex);
        }
        catch (TagException tagEx)
        {
        }
    }

    public String getGenre()
    {
        if ((genreCode >= 0) && (genreCode < genreTable.length))
        {
            return(genreTable[genreCode]);
        }

        if (genreText != null)
        {
            return genreText;
        }
        
        return("Unknown");
    }

    public int getGenreCode()
    {
        return(genreCode);
    }

    public void setGenreCode(int newVal)
    {
        genreCode=newVal;
    }

    public String getTitle()
    {
        return(title);
    }

    public void setTitle(String newVal)
    {
        title=newVal;
    }

    public String getArtist()
    {
        return(artist);
    }

    public void setArtist(String newVal)
    {
        artist=newVal;
    }

    public String getAlbum()
    {
        return(album);
    }

    public void setAlbum(String newVal)
    {
        album=newVal;
    }

    public String getPublishYear()
    {
        return(publishYear);
    }

    public void setPublishYear(String newVal)
    {
        publishYear=newVal;
    }

    public String getComment()
    {
        return(comment);
    }

    public void setComment(String newVal)
    {
        comment=newVal;
    }

    public boolean isTagIncluded()
    {
        return(tagIncluded);
    }

    public boolean isPictureIncluded()
    {
        return containsPicture;
    }
    
    public String[] getGenreList()
    {
        return(genreTable);
    }

    public void store()
    {
        File tmpFile = new File(fileName);

        if ((!tmpFile.exists()) || (!tmpFile.isFile()))
        {
            Logger.getLogger(getClass()).error("MP3 file not found: " + fileName);
            return;
        }

        if (!tmpFile.canWrite())
        {
            Logger.getLogger(getClass()).error("MP3 file is not writable: " + fileName);
            return;
        }
        
        try
        {
            MP3File mp3file = new MP3File(tmpFile);     

            ID3v1 id3v1 = null;
            
            if (mp3file.hasID3v1Tag())
            {
                id3v1 = mp3file.getID3v1Tag();
            }
            else
            {
                id3v1 = new ID3v1();
            }
            
            id3v1.setAlbum(album);
            id3v1.setArtist(artist);
            id3v1.setTitle(title);
            id3v1.setYear(publishYear);
            id3v1.setComment(comment);
            id3v1.setGenre((byte) genreCode);
            
            mp3file.setID3v1Tag(id3v1);

            AbstractID3v2 id3v2 = null;

            if (mp3file.hasID3v2Tag())
            {
                id3v2 = mp3file.getID3v2Tag();
            }
            else
            {
                id3v2 = new ID3v2_2();
            }
            
            id3v2.setSongTitle(title);
            id3v2.setLeadArtist(artist);
            id3v2.setAlbumTitle(album);
            id3v2.setYearReleased(publishYear);
            id3v2.setSongComment(comment);
            id3v2.setSongGenre(getGenre());
            
            mp3file.setID3v2Tag(id3v2);
            
            mp3file.save();
        }
        catch (TagException tagEx)
        {
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("error writing MP3 tag");
        }
    }

}
