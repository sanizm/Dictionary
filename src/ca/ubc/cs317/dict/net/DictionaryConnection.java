package ca.ubc.cs317.dict.net;

import ca.ubc.cs317.dict.model.Database;
import ca.ubc.cs317.dict.model.Definition;
import ca.ubc.cs317.dict.model.MatchingStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


import static ca.ubc.cs317.dict.net.Status.readStatus;

public class DictionaryConnection {

    private static final int DEFAULT_PORT = 2628;
    private final Socket echoSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    /**
     * Establishes a new connection with a DICT server using an explicit host and port number, and handles initial
     * welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @param port Port number used by the DICT server
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     *                                 don't match their expected value.
     */
    public DictionaryConnection(String host, int port) throws DictConnectionException {
        try {
            this.echoSocket = new Socket(host, port);
            // output stream
            this.out = new PrintWriter(echoSocket.getOutputStream(), true);
            // Input stream
            this.in = new BufferedReader(new InputStreamReader(this.echoSocket.getInputStream()));
            // Capture Status
            Status s = readStatus(this.in);
            if (s.getStatusCode() == 420 || s.getStatusCode() == 421 || s.getStatusType() == 5) {
                throw new DictConnectionException();
            }


        } catch (Exception e) {
            throw new DictConnectionException();
        }
    }

    /**
     * Establishes a new connection with a DICT server using an explicit host, with the default DICT port number, and
     * handles initial welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     *                                 don't match their expected value.
     */
    public DictionaryConnection(String host) throws DictConnectionException {
        this(host, DEFAULT_PORT);
    }

    /**
     * Sends the final QUIT message and closes the connection with the server. This function ignores any exception that
     * may happen while sending the message, receiving its reply, or closing the connection.
     */
    public synchronized void close() {
        try {
            this.out.println("QUIT");
            this.in.readLine();
            this.out.close();
            this.in.close();
            this.echoSocket.close();
        } catch (Exception ignored) {

        }


    }

    /**
     * Requests and retrieves a map of database name to an equivalent database object for all valid databases used in the server.
     *
     * @return A map linking database names to Database objects for all databases supported by the server, or an empty map
     * if no databases are available.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Map<String, Database> getDatabaseList() throws DictConnectionException {
        Map<String, Database> databaseMap = new HashMap<>();

        try {
            this.out.println("SHOW db");
            String line = this.in.readLine();

            if (line.split(" ", 2)[0].equals("554"))
                return databaseMap;

            line = this.in.readLine();

            ArrayList<String> al = new ArrayList<>();
            while (line != null) {
                al.add(line);

                line = this.in.readLine();
                if (!this.in.ready()) break;
            }

            for (int i = 0; i < al.size() - 1; i++) {
                String[] splitDatabaseContent = al.get(i).split(" ", 2);

                databaseMap.put(splitDatabaseContent[0], new Database(splitDatabaseContent[0], splitDatabaseContent[1].substring(1, splitDatabaseContent[1].length() - 1)));

            }
        } catch (Exception e) {
            throw new DictConnectionException();
        }

        return databaseMap;
    }

    /**
     * Requests and retrieves a list of all valid matching strategies supported by the server.
     *
     * @return A set of MatchingStrategy objects supported by the server, or an empty set if no strategies are supported.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Set<MatchingStrategy> getStrategyList() throws DictConnectionException {
        Set<MatchingStrategy> set = new LinkedHashSet<>();

        try {
            this.out.println("SHOW STRATEGIES");
            ArrayList<String> al = new ArrayList<>();
            String line = this.in.readLine();
            if (line.split(" ", 2)[0].equals("555") || line.split(" ", 2)[0].equals("501") || line.split(" ", 2)[1].equals("0"))
                return set;

            line = this.in.readLine();


            while (line != null) {

                al.add(line);
                line = this.in.readLine();
                if (!this.in.ready()) break;
            }

            for (int i = 0; i < al.size() - 1; i++) {
                String[] strategyContent = al.get(i).split(" ", 2);
                MatchingStrategy ms = new MatchingStrategy(strategyContent[0], strategyContent[1].substring(1, strategyContent[1].length() - 1));
                set.add(ms);
            }
        } catch (Exception e) {
            throw new DictConnectionException();
        }

        return set;
    }

    /**
     * Requests and retrieves a list of matches for a specific word pattern.
     *
     * @param word     The word whose definition is to be retrieved.
     * @param strategy The strategy to be used to retrieve the list of matches (e.g., prefix, exact).
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 matches in the first database that has a match for the word should be used (database '!').
     * @return A set of word matches returned by the server, or an empty set if no matches were found.
     * @throws DictConnectionException If the connection was interrupted, the messages don't match their expected
     *                                 value, or the database or strategy are invalid.
     */
    public synchronized Set<String> getMatchList(String word, MatchingStrategy strategy, Database database) throws DictConnectionException {
        Set<String> set = new LinkedHashSet<>();

        try {

            this.out.println("MATCH " + database.getName() + " " + strategy.getName() + " \"" + word + "\"");

            String line = this.in.readLine();

            ArrayList<String> al = new ArrayList<>();
            if (line.split(" ", 2)[0].equals("552")) {
                return set;
            }
            if (line.split(" ", 2)[0].equals("550") || line.split(" ", 2)[0].equals("551")) {
                throw new DictConnectionException();
            }

            line = this.in.readLine();


            while (line != null) {
                al.add(line);

                line = this.in.readLine();

                if (!this.in.ready()) break;
            }
            for (int i = 0; i < al.size() - 1; i++) {
                String[] matchContent = al.get(i).split(" ", 2);
                if (matchContent.length > 1)
                    set.add(matchContent[1].substring(1, matchContent[1].length() - 1));

            }

        } catch (Exception e) {
            throw new DictConnectionException(e.getMessage());
        }


        // TODO Add your code here

        return set;
    }

    /**
     * Requests and retrieves all definitions for a specific word.
     *
     * @param word     The word whose definition is to be retrieved.
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 definitions in the first database that has a definition for the word should be used
     *                 (database '!').
     * @return A collection of Definition objects containing all definitions returned by the server, or an empty
     * collection if no definitions were returned.
     * @throws DictConnectionException If the connection was interrupted, the messages don't match their expected
     *                                 value, or the database is invalid.
     */
    public synchronized Collection<Definition> getDefinitions(String word, Database database) throws DictConnectionException {
        Collection<Definition> set = new ArrayList<>();

        StringBuilder sb = null;
        Definition definition;
        boolean isDefinitionsRetrieved = false;
        String wName = "", DbName = "";
        try {

            this.out.println("DEFINE" + " " + database.getName() + " \"" + word + "\"");

            String line = this.in.readLine();

            if (line.split(" ", 2)[0].equals("550")) {
                throw new DictConnectionException();
            }
            if (line.split(" ", 2)[0].equals("552")) {

                return set;
            }

            ArrayList<String> al = new ArrayList<>();
            while (line != null) {
                al.add(line);

                line = this.in.readLine();
                if (!this.in.ready()) break;
            }

            for (int i = 0; i < al.size(); i++) {

                String[] DefinitionContent = al.get(i).split("\"");
                if (DefinitionContent[0].trim().equals("151")) {
                    isDefinitionsRetrieved = true;
                    if (sb != null) {
                        definition = new Definition(wName, DbName);
                        int t = sb.reverse().indexOf(".");
                        sb.deleteCharAt(t);
                        sb.reverse();
                        definition.setDefinition(sb.toString());
                        set.add(definition);
                    }
                    sb = new StringBuilder();
                    wName = DefinitionContent[1].trim();
                    DbName = DefinitionContent[2].trim();
                    continue;
                }
                if (isDefinitionsRetrieved) {
                    sb.append(al.get(i));
                    sb.append("\n");
                }

            }
            if (isDefinitionsRetrieved) {
                definition = new Definition(wName, DbName);
                int t = sb.reverse().indexOf(".");
                sb.deleteCharAt(t);
                sb.reverse();
                definition.setDefinition(sb.toString());
                set.add(definition);
            }

        } catch (Exception e) {
            throw new DictConnectionException(e.getMessage());
        }

        return set;
    }

}
