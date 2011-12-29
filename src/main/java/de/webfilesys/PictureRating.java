package de.webfilesys;

/**
 * Container for picture rating info.
 * @author Frank Hoehnel
 */
public class PictureRating
{
    private int numberOfVotes = 0;
    
    private int averageVisitorRating = (-1);
    
    private int ownerRating = (-1);
    
    public void setNumberOfVotes(int newVal) 
    {
        numberOfVotes = newVal;
    }
    
    public int getNumberOfVotes() 
    {
        return numberOfVotes;
    }
    
    public void setAverageVisitorRating(int newVal)
    {
        averageVisitorRating = newVal;
    }
    
    public int getAverageVisitorRating() 
    {
        return averageVisitorRating;
    }
    
    public void setOwnerRating(int newVal)
    {
        ownerRating = newVal;
    }
    
    public int getOwnerRating() 
    {
        return ownerRating;
    }
}
