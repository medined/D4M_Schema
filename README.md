[![Build Status](https://travis-ci.org/medined/D4M_Schema.svg?branch=master)](https://travis-ci.org/medined/D4M_Schema) - Travis CI
<br/>
[![Build Status](https://api.shippable.com/projects/5385e15ce5ca5fba01621831/badge/master)](https://www.shippable.com/projects/5385e15ce5ca5fba01621831) - Shippable
<br/>
[![Coverage Status](https://coveralls.io/repos/medined/D4M_Schema/badge.png?branch=master)](https://coveralls.io/r/medined/D4M_Schema?branch=master)
<br/>

Check the wiki at <a target='_blank' href='https://github.com/medined/D4M_Schema/wiki'>https://github.com/medined/D4M_Schema/wiki</a>.

A big shout of thanks to the folks at MIT for both writing
the software and helping me to understand the schema. The official D4M website is <a target="blank" href="http://d4m.mit.edu/">http://d4m.mit.edu/</a>. The primary paper describing D4M is <a target="_blank" href="http://www.icassp2012.com/Papers/ViewPapers.asp?PaperNum=2970">http://www.icassp2012.com/Papers/ViewPapers.asp?PaperNum=2970</a>.

There are many articles talking about the benefits of Big Data and how it can 
knock down Data Silos and pool data together into a Data Lake. Most  are short 
on specifics. In this project, I'll use D4M (http:/d4m.mit.edu/) to fill in 
some blanks. Hopefully, you'll find some ideas here you haven't seen before 
and you'll get inspired to knock down some of your own Data Silos. It can done!

>D4M uses a Schema on Read philosophy. Check out <a target='_blank'  href='http://ibmdatamag.com/2013/05/why-is-schema-on-read-so-useful/'>Why is Schema on Read So Useful?</a> for background information.

[Getting Started](docs/getting_started.md)

[Pagination Considerations](docs/pagination.md)

[Who Uses D4M?](docs/d4m_use.md)

[Digging Deeper](docs/digging_deeper.md)

[Data Distribution](docs/data_distribution.md)

[Research Topics](docs/research_topics.md)

D4M's main page says:

<blockquote>D4M is a breakthrough in computer programming that combines the 
advantages of five distinct processing technologies (sparse linear algebra, 
associative arrays, fuzzy algebra, distributed arrays, and triple-store/NoSQL 
databases such as Hadoop HBase and Apache Accumulo) to provide a database and 
computation system that addresses the problems associated with Big 
Data.</blockquote>

For my purpose, a Big Data project has several phases: Extract, Transform, 
Load, and Query. I'm leaving the Extract phase as a reader exercise since 
so much depending on where your data is coming from.

At this point, you might be thinking that I've forgotten to mention 
something -- What is the DBM v2.0 Schema? It is a NO-SQL schema that enables 
the Data Lake. All types of storage can be combined 
into a set of three Accumulo tables that work together. The D4M project 
provides plenty of documentation regarding query performance which I will 
not repeat here.

What is the D4M Schema?
-----------------------------------------

In this simple example, I'm just putting a fact table (cities in Maine) into 
the D4M schema. A UUID is used to group all information about one record 
together. This is a simplistic technique and should not be used in production.
Each field name and field value (a fact) is stored twice - once in the Tedge 
table and once in the TedgeTranspose table. 

* The <i>Tedge</i> table allows rapid access to a whole record. 

* The <i>TedgeTranpose</i> table allows rapid access to every record where a given 
fact is referenced. 

* The <i>TedgeDegree</i> table provides counts for each fact. It can be used for 
faceting.  If your goal is high speed inserts, you need to pre-sum the inserts 
into the <i>TedgeDegree</i> table otherwise this can become a bottleneck.

* The <i>TedgeText</i> table contains the original record. It can save a lot of 
time when you just want pull out the whole record.

* [Extension] The <i>TedgeField</i> table holds a list of columns. After loading data, 
you might find yourself with an unknown number of columns. By storing the column name
and a count of how many entries are in each column, this table aids in Data Exploration.

Below is a concrete example of how these tables are populated:

```
Relational Record
   CITY_NAME | STATE_NAME
   AKRON     | MAINE

Accumulo Mutations
   TABLE          * ROW                              * CQ                               * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
   TedgeDegree    * CITY_NAME|AKRON                  * Degree                           * 1
   TedgeDegree    * STATE_NAME|MAINE                 * Degree                           * 1
   TedgeField     * STATE_NAME                       * field                            * 1
   TedgeField     * CITY_NAME                        * field                            * 1
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeTxt       * 9a127928-b661-4e46-9103-3fc024f4 * RawData                          * CITY_NAME|AKRON
                                                                                          \t
                                                                                          STATE_NAME|MAINE
```

Then see what changes when an additional record for the BOAZ city.

```
Relational Records
   CITY_NAME | STATE_NAME
   AKRON     | MAINE
A  BOAZ      | MAINE

Accumulo Mutations
   TABLE          * ROW                              * CQ                               * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * CITY_NAME|BOAZ                   * 1
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * STATE_NAME|MAINE                 * 1
   TedgeDegree    * CITY_NAME|AKRON                  * degree                           * 1
A  TedgeDegree    * CITY_NAME|BOAZ                   * degree                           * 1
M  TedgeDegree    * STATE_NAME|MAINE                 * degree                           * 2
M  TedgeField     * STATE_NAME                       * field                            * 2
M  TedgeField     * CITY_NAME                        * field                            * 2
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * CITY_NAME|BOAZ                   * a1b4d569-ee45-4466-af2a-0960ccc1 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * STATE_NAME|MAINE                 * a1b4d569-ee45-4466-af2a-0960ccc1 * 1

   TedgeText      * 9a127928-b661-4e46-9103-3fc024f4 * RawData                          * CITY_NAME|AKRON
                                                                                          \t
                                                                                          STATE_NAME|MAINE

A  TedgeText      * a1b4d569-ee45-4466-af2a-0960ccc1 * RawData                          * CITY_NAME|BOAZ
                                                                                          \t
                                                                                          STATE_NAME|MAINE
```

The 'A' lines were added. The 'M' line was modified. The rest stayed the same. 

Can you foresee a problem if some data needs to be re-ingested? Right, since a
UUID value was used, duplicate records would be ingested.

Typically, one or more of the incoming fields are designed 'primary' to 
indicate that when taken together then make the record unique. This field 
designation is specific to each use case.

Our tiny dataset is not well-formed because there might be two cities with the
same name in Maine. If I were the Data Architect on this pretend project, I'd
reject this data until Latitude and Longitude values were added.

Time has passed, geographic information has been added and another ingest 
attempt is happening. This time our row value will be the City name and
the geographic position. We'll ignore the TedgeDegree and TedgeText table 
since they won't change.

```
Relational Record
   CITY_NAME | STATE_NAME | LATITUDE | LONGITUDE
   AKRON     | MAINE      | 43.22    | -70.79
   BOAZ      | MAINE      | 45.25    | -69.44

Accumulo Mutations
   TABLE          * ROW                * CQ                 * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * AKRON|43.22|-70.79 * CITY_NAME|AKRON    * 1
   Tedge          * AKRON|43.22|-70.79 * STATE_NAME|MAINE   * 1 
A  Tedge          * BOAZ|45.25|-69.44  * CITY_NAME|BOAZ     * 1
A  Tedge          * BOAZ|45.25|-69.44  * STATE_NAME|MAINE   * 1
   TedgeTranspose * CITY_NAME|AKRON    * AKRON|43.22|-70.79 * 1
A  TedgeTranspose * CITY_NAME|BOAZ     * BOAZ|45.25|-69.44  * 1
   TedgeTranspose * STATE_NAME|MAINE   * AKRON|43.22|-70.79 * 1
A  TedgeTranspose * STATE_NAME|MAINE   * BOAZ|45.25|-69.44  * 1
A  TedgeField     * LATITUDE           * field              * 1
A  TedgeField     * LONGITUDE          * field              * 1
```

Now we can re-ingest the data as often as needed without creating duplication 
in Accumulo. The new entries would overlay the old entries.

If the row value gets longer than 40 characters, you can hash it into a SHA-1
value. In fact, the CsvReader class in this project has a sha1 flag which
generates a sha1 of the whole record for each of use.

Not withstanding the above suggestion of using SHA-1, good row value design is
an art. Try to design them with these guidelines in mind:

 * Human readable (i.e. actually means something), which makes debugging a lot 
easier.

 * Rapidly changing leading values which provides automatic load balancing and 
easy pre-splitting. For example, reversing an ingest date so that the faster
changing seconds come first.
