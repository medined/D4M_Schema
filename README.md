There are many articles talking about the benefits of Big Data and how it can knock down Data Silos and pool data together into a Data Lake. Most  are short on specifics. In this project, I'll use D4M (from MIT Lincoln Labs - http://www.mit.edu/~kepner/D4M/) to fill in some blanks. Hopefully, you'll find some ideas here you haven't seen before and you'll get inspired to known down some of your own Data Silos. It can done!

D4M's main page says:

<blockquote>D4M is a breakthrough in computer programming that combines the advantages of five distinct processing technologies (sparse linear algebra, associative arrays, fuzzy algebra, distributed arrays, and triple-store/NoSQL databases such as Hadoop HBase and Apache Accumulo) to provide a database and computation system that addresses the problems associated with Big Data.</blockquote>

For my purpose, a Big Data project has several phases: Extract, Transform, Load, and Query. I'm leaving the Extract phase as a reader exercise since so much depending on where your data is coming from.

At this point, you might be thinking that I've forgotten to mention something -- What is the Common Big Data Architecture? CBDA provides a NO-SQL based schema that enables the Data Lake. All types of storage can be combined into a set of three Accumulo tables that work together. The D4M project provides plenty of documentation regarding query performance which I will not repeat here.

What is the Common Big Data Architecture?
-----------------------------------------

For my example, I'm just putting a fact table (cities in Maine) into the D4M schema. There are no text fields so there is no TedgeText table.

RELATIONAL RECORD
   CITY_NAME | STATE_NAME
   AKRON     | MAINE

MUTATIONS
   TABLE          * ROW                              * CF                               * VALUE
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeDegree    * CITY_NAME|AKRON                  * Degree                           * 1
   TedgeDegree    * STATE_NAME|MAINE                 * Degree                           * 1

Then we can add an additional record for the BOAZ city.

RELATIONAL RECORD
   CITY_NAME | STATE_NAME
   AKRON     | MAINE
A  BOAZ      | MAINE

MUTATIONS
   TABLE          * ROW                              * CF                               * VALUE
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * CITY_NAME|BOAZ                   * 1
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * STATE_NAME|MAINE                 * 1
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * CITY_NAME|BOAZ                   * a1b4d569-ee45-4466-af2a-0960ccc1 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * STATE_NAME|MAINE                 * a1b4d569-ee45-4466-af2a-0960ccc1 * 1
   TedgeDegree    * CITY_NAME|AKRON                  * Degree                           * 1
A  TedgeDegree    * CITY_NAME|BOAZ                   * Degree                           * 1
M  TedgeDegree    * STATE_NAME|MAINE                 * Degree                           * 2

The 'A' lines were added. The 'M' line was modified. The rest stayed the same. 

As a refinement, I could designate some of the incoming fields (or a combination of them) as a primary key then take the SHA-1 instead of using a UUID value. Using this refinement, data could be reprocessed if needed.




Getting Started with D4M
------------------------

How to get started? Get D4M working. There are instructions how to create a three-node Accumulo cluster and how to install D4M at https://github.com/medined/Accumulo_1_5_0_By_Vagrant. Once you can start Octave and run ls(DB) to see the tables into Accumulo, you're ready to read further.


ETL
---



