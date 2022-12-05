# Zendesk Multi Objects Batch Source


Description
-----------
This source reads multiple objects from Zendesk. It extracts reportable data from the multiple Zendesk objects. 
The Zendesk Multi Object Batch Source plugin enables bulk data extraction from Zendesk. 
You can configure and execute bulk data transfers from Zendesk without any coding.

Examples of objects are Article Comments, Post Comments, Requests Comments, Ticket Comments,
Groups, Organizations, Satisfaction Ratings, Tags, Ticket Fields,
Ticket Metrics, Ticket Metric Events, Tickets, Users.
Tags object lists the 500 most popular tags in the last 60 days.

The data which should be read is specified using object list and filters for those objects.

In addition, for each Object that will be read, this plugin will set pipeline arguments where the key is 
'multisink.[ObjectName]' and the value is the schema of the Object.

Configuration
-------------

### Basic

**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata.

**Admin Email:** Zendesk admin email.

**API Token:** Zendesk API token. Can be obtained from the Zendesk Support Admin interface.
For information about generating the Zendesk API token, see [Zendesk API Token](https://support.zendesk.com/hc/en-us/articles/226022787-Generating-a-new-API-token-).

**Subdomains:** List of Zendesk [Subdomains](https://support.zendesk.com/hc/en-us/articles/4409381383578-Where-can-I-find-my-Zendesk-subdomain-) to read object from.

**Object to Pull:** Objects to pull from Zendesk API. If not specified, data is pulled from all objects. Default is blank.

**Object to Skip:** Objects to skip from Zendesk API. Default is blank.

**Start Date:** Filter data to include only records that have Zendesk modified date that is greater than 
or equal to the specified date. The date must be provided in the date format:

|              Format              |       Format Syntax       |          Example          |
| -------------------------------- | ------------------------- | ------------------------- |
| Date, time, and time zone offset | YYYY-MM-DDThh:mm:ss+hh:mm | 1999-01-01T23:01:01+01:00 |
|                                  | YYYY-MM-DDThh:mm:ss-hh:mm | 1999-01-01T23:01:01-08:00 |
|                                  | YYYY-MM-DDThh:mm:ssZ      | 1999-01-01T23:01:01Z      |

Start Date is required for batch objects like: Ticket Comments, Organizations, Ticket Metric Events, Tickets, Users.

**End Date:** Filter data to include only records that have Zendesk modified date that is less than 
the specified date. The date must be provided in the date format:

|              Format              |       Format Syntax       |          Example          |
| -------------------------------- | ------------------------- | ------------------------- |
| Date, time, and time zone offset | YYYY-MM-DDThh:mm:ss+hh:mm | 1999-01-01T23:01:01+01:00 |
|                                  | YYYY-MM-DDThh:mm:ss-hh:mm | 1999-01-01T23:01:01-08:00 |
|                                  | YYYY-MM-DDThh:mm:ssZ      | 1999-01-01T23:01:01Z      |

If you enter an End Date and Start Date, the data is modified within a specific time window. 
If no value is provided, no upper bound is applied.

**Satisfaction Ratings Score:** Filter Satisfaction Ratings object to include only records that have a Zendesk score equal to the specified score. 
Only Applicable for Satisfaction Ratings object.

**Table Name Field:** The name of the field that holds the table name. Must not be the name of any table column that
will be read. Defaults to `tablename`.

### Advanced

**Max Retry Count:** Maximum number of retry attempts. Default is 20.


**Connect Timeout:** Maximum time in seconds that connection initialization can take. Default is 300.


**Read Timeout:** Maximum time in seconds that fetching data from the server can take. Default is 300.


Data Type Mappings from Zendesk to CDAP
----------
The following table lists out different Zendesk data types, as well as the
corresponding CDAP types.

| Zendesk type           | CDAP type |
|------------------------|-----------|
| Boolean                | Boolean   |
| DateTime/Time          | string    |
| Decimal                | string    |
| int16/int34/int64/long | Long      |
| String                 | String    |
| Array                  | Array     |
| Record                 | Record    |

Limitations
----------

Zendesk plugin supports two types of pagination: [offset](https://developer.zendesk.com/documentation/developer-tools/pagination/paginating-through-lists-using-offset-pagination/) and [time-based](https://developer.zendesk.com/documentation/ticketing/managing-tickets/using-the-incremental-export-api/#time-based-incremental-exports)  pagination.
Offset and Time-Based Pagination might result in data duplication.

### Data Duplication issues with Zendesk APIs

The Zendesk Multi Batch Source plugin might return duplicate records for offset and time-based exports as mentioned in Zendesk docs.
[Zendesk documentation](https://developer.zendesk.com/documentation/ticketing/managing-tickets/using-the-incremental-export-api/#excluding-duplicate-items)

Objects that support both offset and time-based exports:
Users,
Tickets,
Ticket Metric Events,
Organizations, and
Ticket Comments.

To solve the duplication issue, add the Deduplicate plugin from the Analytics list after the Zendesk Multi Batch Source in the pipeline.

Supported Zendesk Objects
----------

| Objects Name          | Supported Pagination Type  | Endpoint URI (https://{subDomain}.zendesk.com/api/v2/) |
|-----------------------|----------------------------|--------------------------------------------------------|
| Users                 | Offset, Time-based         |incremental/users.json                                                       |
| Tickets               | Offset, Time-based         |incremental/tickets.json                                                       |
| Ticket Metric Events  | Offset, Time-based         |incremental/ticket_metric_events.json                                                       |
| Ticket Metrics        | Offset                     |ticket_metrics.json                                                       |
| Ticket Fields         | Offset                     |ticket_fields.json                                                       |
| Tags                  | Offset                     |tags.json                                                        |
| Satisfaction Ratings  | Offset                     |satisfaction_ratings.json                                                        |
| Organizations         | Offset, Time-based         |incremental/organizations.json                                                        |
| Groups                | Offset                     |groups.json                                                        |    
| Ticket Comments       | Offset, Time-based         |incremental/ticket_events.json?include=comment_events                                                        |
| Request Comments      | Offset                     |requests/{requestId}/comments.json*                                                        |
| Post Comments         | Offset                     |community/users/{userId}/comments.json                                                        |
| Article Comments      | Offset                     |help_center/users/{userId}/comments.json                                                        |

*  The plugin retrieves the user and request lists, and the comments specific to a specific user and request.

Example
----------

Sample data in Object: Groups

| Name      | Id  | Description        |
|-----------|-----|--------------------|
| Jon Doe   | 1   | Test Description   |
| Raj       | 2   | Test Description 2 |

Sample data in Object: Tags

| Name    | Count    |
|---------|----------|
| Jon Doe | 5        |
| Raj     | 6        |

To read data from these two objects, both of them must be entered in the Tables Names field under the Table specification section.

The output of the source will be the following records:


| Name      | Id  | Description        | tablename |
|-----------|-----|--------------------|-----------|
| Jon Doe   | 1   | Test Description   | Groups    |
| Raj       | 2   | Test Description 2 | Groups    |


| Name    | Count    | tablename |
|---------|----------|-----------|
| Jon Doe | 5        | Tags      |
| Raj     | 6        | Tags      |

The plugin will emit two pipeline arguments to provide multi sink plugin with the schema of the output records:

```js
{
 "type": "record",
 "name": "groups",
 "fields": [
   {"name": "name","type": ["string","null"]},
   {"name": "id","type": ["long","null"]},
   {"name": "description","type": ["string","null"]}
 ]
}

{
 "type": "record",
 "name": "tags",
 "fields": [
   {"name": "name","type": ["string", "null"]}, 
   {"name": "count","type": ["long","null"]}
 ]
}
```