There seems to be many articules talking about the benefits of Big Data and how it can knock down Data Silos and pool all data together into a Data Lake. However, most websites are short on specifically how this can be down. In this project, I'll use D4M (from MIT Lincoln Labs - http://www.mit.edu/~kepner/D4M/) to provide some specifics. Hopefully, you'll find some ideas here you haven't seen before and you'll get inspired to known down some of your own Data Silos. It can done!

D4M's main page says:

<blockquote>D4M is a breakthrough in computer programming that combines the advantages of five distinct processing technologies (sparse linear algebra, associative arrays, fuzzy algebra, distributed arrays, and triple-store/NoSQL databases such as Hadoop HBase and Apache Accumulo) to provide a database and computation system that addresses the problems associated with Big Data.</blockquote>


For my purpose, a Big Data project has several phases: Extract, Transform, Load, and Query. I'm leaving the Extract phase as a reader exercise since so much depending on where your data is coming from.

At this point, you might be thinking that I've forgotten to mention something -- What is the Common Big Data Architecture? CBDA provides a NO-SQL based schema that enables the Data Lake. All types of storage can be combined into a set of three Accumulo tables that work together. The D4M project provides plenty of documentation regarding query performance which I will not repeat here.

Getting Started with D4M
------------------------

How to get started? Get D4M working. There are instructions how to create a three-node Accumulo cluster and how to install D4M at https://github.com/medined/Accumulo_1_5_0_By_Vagrant. Once you can start Octave and run ls(DB) to see the tables into Accumulo, you're ready to read further.


ETL
---



