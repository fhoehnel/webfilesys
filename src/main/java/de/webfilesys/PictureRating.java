package de.webfilesys;

/**
 * Container for picture rating info.
 * @author Frank Hoehnel
 */
public class PictureRating
{
    private int numberOfVotes = 0;
    
    private int averageVisitorRating = (-1);
    
    private float averageVoteVal = 0;
    
    private int ownerRating = (-1);
    
    private int visitorRatingSum = (-1);
    
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
    
    public void setAverageVoteVal(float newVal) {
    	averageVoteVal = newVal;
    }
    
    public float getAverageVoteVal() {
    	return averageVoteVal;
    }
    
    public void setOwnerRating(int newVal)
    {
        ownerRating = newVal;
    }
    
    public int getOwnerRating() 
    {
        return ownerRating;
    }
    
    public void setVisitorRatingSum(int newVal) {
    	visitorRatingSum = newVal;
    }
    
    public int getVisitorRatingSum() {
    	return visitorRatingSum;
    }
}
