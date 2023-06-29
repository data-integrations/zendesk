# Zendesk Batch Source


Description
-----------
The Zendesk Batch Source plugin lets you efficiently extract reportable data from Zendesk objects. 

Configuration
-------------

### Basic

**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata.

**Object to Pull:** Objects to pull from Zendesk API. Default is Groups.

**Start Date:** Filter data to include only records that have a Zendesk modified date that is greater than
or equal to the specified date. The date must be provided in the date format:

|              Format              |       Format Syntax       |          Example          |
| -------------------------------- | ------------------------- | ------------------------- |
| Date, time, and time zone offset | YYYY-MM-DDThh:mm:ss+hh:mm | 1999-01-01T23:01:01+01:00 |
|                                  | YYYY-MM-DDThh:mm:ss-hh:mm | 1999-01-01T23:01:01-08:00 |
|                                  | YYYY-MM-DDThh:mm:ssZ      | 1999-01-01T23:01:01Z      |

Start Date is required for batch objects like: Ticket Comments, Organizations, Ticket Metric Events, Tickets, Users.

**End Date:** Filter data to include only records that have a Zendesk modified date that is less than
the specified date. The date must be provided in the date format:

|              Format              |       Format Syntax       |          Example          |
| -------------------------------- | ------------------------- | ------------------------- |
| Date, time, and time zone offset | YYYY-MM-DDThh:mm:ss+hh:mm | 1999-01-01T23:01:01+01:00 |
|                                  | YYYY-MM-DDThh:mm:ss-hh:mm | 1999-01-01T23:01:01-08:00 |
|                                  | YYYY-MM-DDThh:mm:ssZ      | 1999-01-01T23:01:01Z      |

If you enter an End Date and Start Date, the data is modified within a specific time window.
If no value is provided, no upper bound is applied.

**Satisfaction Ratings Score:** Filter Satisfaction Ratings object to include only records that have a Zendesk score
equal to the specified score. Only applicable for the Satisfaction Rating object.

### Connection

**Use Connection:** Whether to use a connection. If a connection is used, you do not need to provide the credentials.

**Connection:** Name of the connection to use. Object Name information will be provided by the connection.
You also can use the macro function ${conn(connection-name)}.

**Admin Email:** Zendesk admin email.

**API Token:** Zendesk API token. Can be obtained from the Zendesk Support Admin interface.
For API Token generation, see the [Zendesk documentation](https://support.zendesk.com/hc/en-us/articles/226022787-Generating-a-new-API-token-).

**Subdomains:** List of Zendesk Subdomains to read object from.

**Max Retry Count:** Maximum number of retry attempts. Default is 20.

**Connect Timeout:** Maximum time in seconds for connection initialization. Default is 300.

**Read Timeout:** Maximum time in seconds to fetch data from the server. Default is 300.

Data Type Mappings from Zendesk to CDAP
----------
The following table lists out different Zendesk data types, as well as the
corresponding CDAP types.

| Zendesk type           | CDAP type |
|------------------------|-----------|
| Boolean                | boolean   |
| DateTime/Time          | string    |
| Decimal                | string    |
| int16/int34/int64/long | long      |
| String                 | string    |
| Array                  | array     |
| Record                 | record    |


Pagination
----------
Zendesk plugin supports two types of pagination: [offset](https://developer.zendesk.com/documentation/developer-tools/pagination/paginating-through-lists-using-offset-pagination/) 
and [time-based](https://developer.zendesk.com/documentation/ticketing/managing-tickets/using-the-incremental-export-api/#time-based-incremental-exports)  pagination.

Offset and Time-Based Pagination might result in data duplication.
[Zendesk documentation](https://developer.zendesk.com/documentation/ticketing/managing-tickets/using-the-incremental-export-api/#excluding-duplicate-items)  
To solve the duplication issue, add the Deduplicate plugin from the Analytics list after the Zendesk Batch Source in the pipeline.

Supported Zendesk Objects
----------

| Objects Name          | Supported Pagination Type  | Endpoint URI (https://{subDomain}.zendesk.com/api/v2/) | Notes
|-----------------------|----------------------------|--------------------------------------------------------|------------------------------------------------------------------------------|
| Users                 | Time-based                 |incremental/users.json                                  |                    |
| Tickets               | Time-based                 |incremental/tickets.json                                |                      |
| Ticket Metric Events  | Time-based                 |incremental/ticket_metric_events.json                   |                                     |
| Ticket Metrics        | Offset                     |ticket_metrics.json                                     |                  |
| Ticket Fields         | Offset                     |ticket_fields.json                                      |                |
| Tags                  | Offset                     |tags.json                                               | Tags object lists the 500 most popular tags in the last 60 days.        |
| Satisfaction Ratings  | Offset                     |satisfaction_ratings.json                               |                         |
| Organizations         | Time-based                 |incremental/organizations.json                          |                              |
| Groups                | Offset                     |groups.json                                             |           |    
| Ticket Comments       | Time-based                 |incremental/ticket_events.json?include=comment_events   |                                                     |
| Request Comments      | Offset                     |requests/{requestId}/comments.json                      | Retrieve all requests and iterate over request IDs to obtain corresponding request comments.                            |
| Post Comments         | Offset                     |community/users/{userId}/comments.json                  | Retrieve all users and iterate over user IDs to obtain the corresponding post comments.                                    |
| Article Comments      | Offset                     |help_center/users/{userId}/comments.json                | Retrieve all users and iterate over user IDs to obtain the corresponding article comments.


