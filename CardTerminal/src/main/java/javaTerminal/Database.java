package javaTerminal;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.logging.Logger;
import java.util.logging.Level;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class Database {
    private static Database database = new Database();
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> collection;

    private Database(){}

    public static Database getInstance(){
        return database;
    }

    public void initDB(){
        mongoClient = MongoClients.create();
        db = mongoClient.getDatabase("Student");
        collection = db.getCollection("Class");
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

    }

    public void insertOne(Document document){
        collection.insertOne(document);
    }

    public Document find(Document filter){
        return (Document) collection.find(filter);
    }


    public void findAndUpdate(Document filter, Bson update){
        collection.findOneAndUpdate(filter, update);

    }
}
