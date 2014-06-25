
- [Data Distribution Throughout the Accumulo Cluster](#user-content-data-distribution-throughout-the-accumulo-cluster)
	- [Tables](#user-content-tables)
	- [Splits](#user-content-splits)
		- [Adding Splits](#user-content-adding-splits)
			- [First Split](#user-content-first-split)
			- [Tablet Movement](#user-content-tablet-movement)
			- [Second Split](#user-content-second-split)
	- [What is a Key?](#user-content-what-is-a-key)
	- [Using Shards To Split a Row](#user-content-using-shards-to-split-a-row)
		- [When an Accumulo table is created](#user-content-when-an-accumulo-table-is-created)
			- [Coin Flip Sharding](#user-content-coin-flip-sharding)
			- [HASH + MOD Sharding (using natural key)](#user-content-hash--mod-sharding-using-natural-key)
			- [HASH + MOD Sharding (using synthetic key)](#user-content-hash--mod-sharding-using-synthetic-key)

# Data Distribution Throughout the Accumulo Cluster

  This document answers these questions:

 * What is a tablet?
 * What is a split point?
 * What is needed before data can be distributed?

A distributed database typically is thought of as having data spread across multiple servers. But how does the data spread out? That's a question I hope to answer - at least for Accumulo.

At a high level of abstraction, the concept is simple. If you have two servers, then 50% of the data should go to server one and 50% should go to server two. The examples below give concrete demonstrations of data distribution.

Accumulo stores information as key-value pairs (or entries). For a visual reference, below is an empty key-value pair. 

```
-----------  ---------
| key     |  | value |
-----------  ---------
| <nothing here yet> |
-----------  ---------
```

## Tables

A collecton of key-values is called a table. This table is different from one
found in a relational database because there is no schema associated with it.

>What is a Key? See below.

Note: Understanding the difference between a relational database and a 
key-value database is beyond the scope of this discussion. If you want, you
can think of the "key" in this discussion as a primary key. But, fair warning,
that is a false analogy. One which you'll need to forget as you gain more
proficiency with key-value databases. 

A new Accumulo table has a single unit of storage called a tablet. When created, the tablet is empty. As more entries are inserted into a table, Accumulo may 
automatically decide to split the initial tablet into two tablets. As the size 
of the table continues to grow, the split operation is repeated. Or you can 
specify how the splitting occurs. We'll discuss this further below.

>Tables have one or more tablets.

Below is an empty table. For convenience, we'll use 'default' as the name of 
the initial tablet.

```
-----------  -----------  ---------
| tablet  |  | key     |  | value |
-----------  -----------  ---------
| default |  | <nothing here yet> |
-----------  -----------  ---------
```

Even though the table is empty, it still has a starting key of -infinity and
an ending key of +infinity. All possible data occurs between the two extremes of infinity.

```
  -infinity ==> ALL DATA  <== +infinity.
```

This concept of start and end keys can be shown in our tablet depiction as 
well.

```
-----------  -----------  ---------
| tablet  |  | key     |  | value |
-----------  -----------  ---------
|      start key: -infinity       |
-----------------------------------
| default |  | <nothing here yet> |
-----------------------------------
|        end key: +infinity       |
-----------  -----------  ---------
```

After inserting three records into a new table, you'll have the following 
situaton. Notice that Accumulo always stores keys in lexically sorted order.
So far, the start and end keys have not been changed.

```
-----------  -------  ---------
| tablet  |  | key |  | value |
-----------  -------  ---------
| default |  | 01  |  | X     |
| default |  | 03  |  | X     |
| default |  | 05  |  | X     |
-----------  -------  ---------
```

Accumulo stores all entries for a tablet on a single node in the clsuter. Since our
table has only one tablet, the information can't spread beyond one node. In 
order to distribute information, you'll need to create more than tablet for
your table.

> The tablet's range is still from -infinity to +infinity. That hasn't changed yet.

## Splits

Now we can introduce the idea of splits. When a tablet is split, one tablet
becomes two. If you want your information to be spread onto three nodes, you'll
need two splits. We'll illustrate this idea.

>Split point - the place where one tablet becomes two.

Let's add two split pointsto see what happens. As the split points are added, new tablets are created.

### Adding Splits

#### First Split

First, adding split point 02 results in a second tablet being created. It's worth noting that the tablet names are meaningless. Accumulo assigns internal names that you rarely need to know. I picked "A" and "B" because they are easy to read.

```
-----------  -------  ---------
| tablet  |  | key |  | value |
-----------  -------  ---------
| A       |  | 01  |  | X     | range: -infinity to 02 (inclusive)
|       split point 02        |
| B       |  | 03  |  | X     | range: 02 (exclusive) to +infinity
| B       |  | 05  |  | X     | 
-----------  -------  ---------
```

The split point does not need to exist as an entry. This feature means that you can pre-split a table by simply giving Accumulo a list of split points.

#### Tablet Movement

Before continuing, let's take a small step back to see how tablets are moved between servers. At first, the table resides on one server. This makes sense - one tablet is on one server.

```
--------------------------------
| Tablet Server                |
--------------------------------
|                              |
|  -- Tablet ----------------  |
|  | -infinity to +infinity |  |
|  --------------------------  |
|                              |
--------------------------------
```

Then the first split point is added. Now there are two tablets. However, they are still on a single server. And this also makes sense. Thinking about adding a split point to a table with millions of entries. While the two tablets reside on one server, adding a split is just an accounting change. 

```
-----------------------------------------------------------------------
| Tablet Server                                                       |
-----------------------------------------------------------------------
|                                                                     |
|  -- Tablet ---------------------   -- Tablet ---------------------  |
|  | -infinity to 02 (inclusive) |   | 02 (exclusive) to +infinity |  |
|  -------------------------------   -------------------------------  |
|                                                                     |
-----------------------------------------------------------------------
```

At some future point, Accumulo might move the second tablet to another Tablet Server. 

```
------------------------------------|  |------------------------------------
| Tablet Server                     |  | Tablet Server                     |
------------------------------------|  |------------------------------------
|                                   |  |                                   |
|  -- Tablet ---------------------  |  |  -- Tablet ---------------------  |
|  | -infinity to 02 (inclusive) |  |  |  | 02 (exclusive) to +infinity |  |
|  -------------------------------  |  |  -------------------------------  |
|                                   |  |                                   |
-------------------------------------  -------------------------------------
```

#### Second Split

You'll wind up with three tablets when a second split point of "04" is added.

```
-----------  -------  ---------
| tablet  |  | key |  | value |
-----------  -------  ---------
| A       |  | 01  |  | X     | range: -infinity to 02 (inclusive)
|       split point 02        |
| B       |  | 03  |  | X     | range: 02 (exclusive) to 04 (inclusive)
|       split point 04        |
| C       |  | 05  |  | X     | range: 04 (exclusive) to +infinity
-----------  -------  ---------
```

The table now has three tablets. When enough tablets are created, some process 
inside Accumulo moves one or more tablets into different nodes. Once that 
happens the data is distributed.

Hopefully, you can now figure out which tablet any specific key inserts into.
For example, key "00" goes into tablet "A".

```
-----------  -------  ---------
| tablet  |  | key |  | value |
-----------  -------  ---------
| A       |  | 00  |  | X     | range: -infinity to 02 (inclusive)
| A       |  | 01  |  | X     |
|       split point 02        |
| B       |  | 03  |  | X     | range: 02 (exclusive) to 04 (inclusive)
|       split point 04        |
| C       |  | 05  |  | X     | range: 04 (exclusive) to +infinity
-----------  -------  ---------
```

Internally, the first tablet ("A") as a starting key of -infinity. Any entry 
with a key between -infinity and "00" inserts into the first key. The last 
tablet has an ending key of +infinity. Therefore any key between "05" and 
+infinity inserts into the last tablet.

Accumulo automatically creates split points based on some conditions. For example, if the tablet grows too large. However, that's a whole 'nother conversation.

## What is a Key?

Plenty of people have described Accumulo's Key layout. Here is the
bare-bones explanation:

```
-------------------------------------------------------------------
| row | column family | column qualifier | visibility | timestamp |
-------------------------------------------------------------------
```

These five components, combined, go into the _Key_.

## Using Shards To Split a Row

Each row resides on a single tablet which can cause a problem if any 
single row has a few million entries. For example, if your table held 
all ISBN's using this schema:

```
------------------------------------------------
| row | column family | column qualifier       |
------------------------------------------------
| book | 140122317    | Batman: Hush           |
| book | 1401216676   | Batman: A Killing Joke |
```

You can see how the _book_ row would have millions of entries. Potentially 
causing memory issues inside your TServer. Many people add a _shard_ value 
to the row to introduce potential split points. With shard values, the 
above table might look like this:

```
---------------------------------------------------
| row    | column family | column qualifier       |
---------------------------------------------------
| book_0 |  140122317    | Batman: Hush           |
| book_5 |  1401216676   | Batman: A Killing Joke |
```

With this style of row values, Accumulo could use book_5 as a split point 
so that the row is no longer unmanageable. Of course, this technique adds a 
bit of complexity to the query process. I'll leave the query issue to a 
future note.

It's also possible to place the shard at the beginning of the row value. With
this technique, the table would like like:

```
---------------------------------------------------
| row    | column family | column qualifier       |
---------------------------------------------------
| 0_book |  140122317    | Batman: Hush           |
| 5_book |  1401216676   | Batman: A Killing Joke |
```

Using the shard component as a prefix does accomplish the goal of providing
Accumulo with a potential split point. However, you'd need to use a 
_BatchScanner_ to find all of the 'book' entries instead of using a 
_Scanner_. Which approach you take totally depends on your situation.

I prefer using the shard component as a suffix so that _Scanner_ can be 
used. I feel this approach coincides nicely with the TedgeTranspose table 
of the D4M schema.

Let's explore how shard values can be generated.

### When an Accumulo table is created

It may be tempting to have the computers flip a virtual coin to decide which
server to target for each record. In the RDBMS world that procedure works but
in key-value databases, information is stored vertically instead of 
horizontally so the coin flip analogy does not work. Let's quickly review why.

#### Coin Flip Sharding

Relational databases spread information across columns (i.e., horizontally). Hopefully, there is in Id value using a synthetic key (SK) and I hope you have them in your data. If not your very first task is to get your DBA's to add  them. Seriously, synthetic keys save you a world of future trouble. Here is a simple relational record.

```
|--------------------------------------
| RELATIONAL REPRESENTATION           |
|--------------------------------------
| SK   | First Name | Last Name | Age |
|-------------------------------------|
| 1001 | John       | Kloplick  | 36  |
---------------------------------------
```

Key-value database spread information across several rows using the synthetic key to tie them together. In simplified form, the information is stored in three key-value combinations (or three entries).

```
|----------------------------------
| KEY VALUE REPRESENTATION        |
|----------------------------------
| ROW  | CF         | CQ          |
|---------------------------------|
| 1001 | first_name | John        |
| 1001 | last_name  | Kloplick    |
| 1001 | age        | 36          |
-----------------------------------
```

If the coin flip sharding strategy were used the information might look like the following. The potential split point shows that the entries can be spread across two tablets.

```
|-------------------------------------
| ROW     | CF         | CQ          |
|------------------------------------|
| 1001_01 | first_name | John        |
| 1001_01 | age        | 36          |
| 1001_02 | last_name  | Kloplick    | <-- potential split point
--------------------------------------
```

To retrieve the information you'd need to scan both servers! This coin flip sharding technique is not going to scale. Imagine information about a person spread over 40 servers. Collating that information would be prohibitively time-consuming.

#### HASH + MOD Sharding (using natural key)

Of course, there is a better sharding strategy to use. You can base the strategy on one of the fields. Get its hash code and then mod it by the number of partitions. Ultimately, this strategy will fail but let's go through the process to see why. Skip to the next section if you already see the problem.

"John".hashCode() is 2314539. Then we can mod that by the number of partitions (or servers) in our cluster. Let's pretend we have 5 servers instead of the two we used earlier for variety. Our key-value entries now look thusly:

> 2,314,539 modulo 5 = 4

```
|-------------------------------------
| ROW     | CF         | CQ          |
|------------------------------------|
| John_04 | first_name | John        |
| John_04 | age        | 36          |
| John_04 | last_name  | Kloplick    |
--------------------------------------
```

> Note that the shard value is _not_ related to any specific node. It's just a potential split point for Accumulo.

It's time to look at a specific use case to see if this sharding strategy is sound. What if we need to add a set of friends for John? It's unlikely that the information about John's friends have his first name. But very likely for his synthetic key of 1001 to be there. We can now see choosing the first_name field as the base of the sharding strategy was unwise.

#### HASH + MOD Sharding (using synthetic key)

Using the synthetic key as the basis for the hash provides more continuity between updates. And regardless of how information changes, we'll always put the information in the same shard. 

"1001".hashCode() is 1507424. If we use the first prime number less than 1,000 then the shard calculation generates a shard value of 957.

So the key-value information is now:

> 1,507,424 modulo 997 = 957

```
|--------------------------------------
| ROW      | CF         | CQ          |
|-------------------------------------|
| 1001_957 | first_name | John        |
| 1001_957 | age        | 36          |
| 1001_957 | last_name  | Kloplick    |
--------------------------------------
```

Using this technique makes it simple to add a height field. 

```
|--------------------------------------------
| ROW      | CF               | CQ          |
|-------------------------------------------|
| 1001_957 | height_in_inches | 68          |
---------------------------------------------
```

