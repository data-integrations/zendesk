# Zendesk Connection

Description
-----------
Use this connection to access data in Zendesk.

Properties
----------
**Name:** Name of the connection. Connection names must be unique in a namespace.

**Description:** Description of the connection.

**Admin Email:** Zendesk admin email.

**API Token:** Zendesk API token. Can be obtained from the Zendesk Support Admin interface.
For API Token generation, see the [Zendesk documentation](https://support.zendesk.com/hc/en-us/articles/226022787-Generating-a-new-API-token-).

**Subdomains:** List of Zendesk Subdomains to read object from.

**Max Retry Count:** Maximum number of retry attempts. Default is 20.

**Connect Timeout:** Maximum time in seconds for connection initialization. Default is 300.

**Read Timeout:** Maximum time in seconds to fetch data from the server. Default is 300.


Path of the connection
----------------------
To browse, get a sample from, or get the specification for this connection (Not supported Zendesk Multi Object batch 
source).  
/{object} This path indicates a Zendesk object. A object is the only one that can be sampled.