import java.security.MessageDigest;
import java.util.Date;
import java.util.ArrayList;
import java.sql.*;

class Block {
    public String hash;
    public String previousHash;
    protected String data; //our data will be a simple message.
    protected long timeStamp; //as number of milliseconds since 1/1/1970.
    private int nonce;

    //Block Constructor.
    public Block(String data,String previousHash ) 
    {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash(); 
    }
    
    public String calculateHash() 
    {
        String calculatedhash = StringUtil.applySha256(previousHash+Long.toString(timeStamp) +Integer.toString(nonce) +data);
        return calculatedhash;
    }
    public void mineBlock(int difficulty) 
    {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0" 
        while(!hash.substring( 0, difficulty).equals(target)) 
        {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
}
class StringUtil 
{
    public static String applySha256(String input)
    {       
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");            
             
            byte[] hash = digest.digest(input.getBytes("UTF-8"));           
            StringBuffer hexString = new StringBuffer(); 
            for (int i = 0; i < hash.length; i++) 
            {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) 
        {
            throw new RuntimeException(e);
        }
    }   
}

public class noobchain 
{

    public static ArrayList<Block> blockchain = new ArrayList<Block>(); 
    public static int difficulty = 5;

    public static void main(String[] args)
    {
        //add our blocks to the blockchain ArrayList:
        blockchain.add(new Block("Hi im the first block", "0"));    
        System.out.println("Trying to Mine block 1... ");
        blockchain.get(0).mineBlock(difficulty);    

        blockchain.add(new Block("Yo im the second block",blockchain.get(blockchain.size()-1).hash));
        System.out.println("Trying to Mine block 2... ");
        blockchain.get(1).mineBlock(difficulty); 

        blockchain.add(new Block("Hey im the third block",blockchain.get(blockchain.size()-1).hash));
        System.out.println("Trying to Mine block 3... ");
        blockchain.get(2).mineBlock(difficulty); 
        
        for(int i=0;i<3;i++)
        {
            System.out.println("\nHash of Block "+(i+1)+" is "+blockchain.get(i).hash);
            
        }
        
        if(isChainValid()==true)
        {
            System.out.println("BlockChain is Valid");
            try{  
                Class.forName("com.mysql.jdbc.Driver");    
              
                Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","");       
                PreparedStatement stmt=con.prepareStatement("insert into blockchain values(?,?,?,?)");
                for(int i=0;i< blockchain.size();i++)
                {
                stmt.setString(1,blockchain.get(i).hash);//1 specifies the first parameter in the query  
                stmt.setString(2,blockchain.get(i).previousHash);
                stmt.setString(3,blockchain.get(i).data);
                stmt.setLong(4,blockchain.get(i).timeStamp);
                stmt.addBatch();
                }
                stmt.executeBatch();  
                System.out.println(" records inserted");  
                  
                con.close();  
                  
            }catch(Exception e)
            { 
                System.out.println(e);
            } 
        }
        else
        System.out.println("BlockChain is InValid");    
    }

    public static Boolean isChainValid() 
    {
        Block currentBlock; 
        Block previousBlock;
        
        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) 
        {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) )
            {
                System.out.println("Current Hashes not equal");         
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) 
            {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }
}
