# Dictionary
A simple dictionary application made using JAVA. The dictionary functionality are provided through a graphical user interface  by implementing a subset of Dictionary protocol referred from RFC  2229.

# Dictionary Preview


https://user-images.githubusercontent.com/75959959/166122251-6d59941f-c2e2-42f2-aa67-a865c2cce355.mp4

# Gist of the application
* JAVA socket is used to create the Dictionary client. the application is implemented using the DICT protocol referred from RFC 2229 which can be found here: https://tools.ietf.org/html/rfc2229.
* Initally a connection with the DICT server is established.
* Requesting, receiving and parsing and returning a list of databases used in the server. Each database corresponds to one dictionary that can be used to retrieve definitions from.
* Requesting, receiving, parsing and returning a list of matching strategies supported by the server. The protocol allows a client to retrieve a list of matches (suggestions) based on a keyword, and the strategy is used to indicate how these keywords are used to present actual dictionary entries. 
* Requesting, receiving, parsing and returning a list of matches based on a keyword, a matching strategy and a database.
* Requesting, receiving, parsing and returning a list of definitions for a word, based on a database. Each definition will correspond to the word, a database, and the definition itself, which may contain several lines.
