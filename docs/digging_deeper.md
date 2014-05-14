Digging Deeper
--------------

The mathematicians have probably already noticed that the D4M tables have
the following properties:

* Tedge and TedgeTranspose - the number of entries equals total number of entries.
* TedgeDegree - the number of entries equals the number of ingested columns.
* TedgeText - the number of entries equals the number of ingested rows.

These three values are the fundamental dimensions of the sparse matrix that 
is represented by the D4M schema.

<blockquote><b>D4M Baseball Example</b> - Dylan Hutchison created a Github 
project at https://github.com/denine99/d4mBB with a self-contained and tested 
Matlab script showing the power of D4M on historical Baseball data. There 
are three parts:

    Parsing data into a form ready for ingestion
    Ingesting data into memory or an Accumulo Table
    Querying data to answer several questions of interest

The goal is to provide a complete, well-documented example demonstrating how 
to use D4M for data analysis on a data set of manageable size. The techniques 
presented will scale.</blockquote>
