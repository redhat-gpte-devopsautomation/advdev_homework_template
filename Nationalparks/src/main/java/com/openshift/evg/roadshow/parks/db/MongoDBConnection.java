package com.openshift.evg.roadshow.parks.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.openshift.evg.roadshow.parks.model.Park;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmorales on 11/08/16.
 */
@Component
public class MongoDBConnection {

private static final String FILENAME = "/nationalparks.json";
private static final String COLLECTION = "nationalparks";

private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

@Autowired
private ResourceLoader resourceLoader;

@Autowired
private Environment env;

private MongoDatabase mongoDB = null;

public MongoDBConnection() {
}

@PostConstruct
public void initConnection() {
        // Get Connection Information from Environment (Config Map)
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String dbName = System.getenv("DB_NAME");
        String dbReplicaSet = System.getenv("DB_REPLICASET");

        logger.info("DB_HOST=" + dbHost);
        logger.info("DB_PORT=" + dbUsername);
        logger.info("DB_USERNAME=" + dbUsername);
        logger.info("DB_PASSWORD=" + dbPassword);
        logger.info("DB_NAME=" + dbName);
        logger.info("DB_REPLICASET=" + dbReplicaSet);

        try {
                String mongoURI = null;
                if (dbReplicaSet == null) {
                        // Single Database Instance
                        mongoURI = "mongodb://" + dbUsername + ":" + dbPassword + "@" + dbHost + ":" + dbPort + "/?authSource=" + dbName;
                }
                else {
                        // Replica Set
                        mongoURI = "mongodb://" + dbUsername + ":" + dbPassword + "@" + dbHost + ":" + dbPort + "/?authSource=" + dbName + "&replicaSet=" + dbReplicaSet;
                }
                logger.info("Connection string: " + mongoURI);
                MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURI));
                mongoDB = mongoClient.getDatabase(dbName);

        } catch (Exception e) {
                logger.error("Creating the mongoDB. " + e.getMessage());
                mongoDB = null;
        }
}

/*
 * Load from embedded list of parks using FILENAME
 */
public List<Document> loadParks() {
        logger.debug("MongoDBConnection.loadParks()");

        try {
                return loadParks(resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + FILENAME).getInputStream());
        } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error loading parks. Return empty list. " + e.getMessage());
        }
        return new ArrayList<Document>();
}

public List<Document> loadParks(String fileLocation) {
        logger.debug("MongoDBConnection.loadParks(" + fileLocation + ")");

        try {
                return loadParks(new FileInputStream(new File(fileLocation)));
        } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error loading parks. Return empty list. " + e.getMessage());
        }
        return new ArrayList<Document>();
}

public List<Document> loadParks(InputStream is) {
        logger.debug("MongoDBConnection.loadParks(InputStream)");

        List<Document> docs = new ArrayList<Document>();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
                String currentLine = null;
                int i = 1;
                while ((currentLine = in.readLine()) != null) {
                        String s = currentLine.toString();
                        Document doc = Document.parse(s);
                        docs.add(doc);
                }
        } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error loading parks. Return empty list. " + e.getMessage());
        } finally {
                try {
                        is.close();
                } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Error loading parks. Return empty list");
                }
        }
        return docs;
}


/**
 *
 */
public void clear() {
        logger.debug("MongoDBConnection.clear()");
        if (mongoDB != null) {
                try{
                        mongoDB.getCollection(COLLECTION).drop();
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB. " + e.getMessage());
                }
        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
}


/**
 * @param parks
 */
public void init(List<Document> parks) {
        logger.debug("MongoDBConnection.init(...)");
        if (mongoDB != null) {
                try {
                        mongoDB.getCollection(COLLECTION).drop();
                        mongoDB.getCollection(COLLECTION).insertMany(parks);
                        mongoDB.getCollection(COLLECTION).createIndex(new BasicDBObject().append("coordinates", "2d"));
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB: " + e.getMessage());
                }
        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
}

/**
 * @return
 */
public long sizeInDB() {
        long size = 0;

        if (mongoDB != null) {
                try {
                        size = mongoDB.getCollection(COLLECTION).count();
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB: " + e.getMessage());
                }

        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
        return size;
}

/**
 * @param parks
 */
public void insert(List<Document> parks) {
        if (mongoDB != null) {
                try {
                        mongoDB.getCollection(COLLECTION).insertMany(parks);
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB. " + e.getMessage());
                }
        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
}

/**
 * @return
 */
public List<Park> getAll() {
        logger.debug("MongoDBConnection.getAll()");
        ArrayList<Park> allParksList = new ArrayList<Park>();

        if (mongoDB != null) {
                try {
                        MongoCollection parks = mongoDB.getCollection(COLLECTION);
                        MongoCursor<Document> cursor = parks.find().iterator();
                        try {
                                while (cursor.hasNext()) {
                                        allParksList.add(ParkReadConverter.convert(cursor.next()));
                                }
                        } finally {
                                cursor.close();
                        }
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB. " + e.getMessage());
                }
        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
        return allParksList;
}

public List<Park> getWithin(float lat1, float lon1, float lat2, float lon2) {
        logger.debug("MongoDBConnection.getAll()");
        ArrayList<Park> allParksList = new ArrayList<Park>();

        if (mongoDB != null) {
                try {
                        MongoCollection parks = mongoDB.getCollection(COLLECTION);
                        // make the query object
                        BasicDBObject spatialQuery = new BasicDBObject();
                        ArrayList<double[]> boxList = new ArrayList<double[]>();
                        boxList.add(new double[] {new Float(lat2), new Float(lon2)});
                        boxList.add(new double[] {new Float(lat1), new Float(lon1)});
                        BasicDBObject boxQuery = new BasicDBObject();
                        boxQuery.put("$box", boxList);
                        spatialQuery.put("pos", new BasicDBObject("$within", boxQuery));
                        logger.info("Using spatial query: " + spatialQuery.toString());

                        MongoCursor<Document> cursor = parks.find(spatialQuery).iterator();
                        try {
                                while (cursor.hasNext()) {
                                        allParksList.add(ParkReadConverter.convert(cursor.next()));
                                }
                        } finally {
                                cursor.close();
                        }
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB. " + e.getMessage());
                }

        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
        return allParksList;
}

/**
 * @param query
 * @return
 */
public List<Park> getByQuery(BasicDBObject query) {
        logger.debug("MongoDBConnection.getByQuery()");
        List<Park> parks = new ArrayList<Park>();
        if (mongoDB != null) {
                try {
                        MongoCursor<Document> cursor = mongoDB.getCollection(COLLECTION).find(query).iterator();
                        int i = 0;
                        try {
                                while (cursor.hasNext()) {
                                        parks.add(ParkReadConverter.convert(cursor.next()));
                                }
                        } finally {
                                cursor.close();
                        }
                } catch (Exception e) {
                        logger.error("Error connecting to MongoDB. " + e.getMessage());
                }

        } else {
                logger.error("mongoDB could not be initiallized. No operation with DB will be performed");
        }
        return parks;
}
}
