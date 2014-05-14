
Pagination in Accumulo
----------------------

Pagination in Accumulo is not simple. Pages are not deterministic since the 
data can be constantly changing. Also authorization levels can change the 
number of returned results. Another consideration is that Accumulo tables 
can only be scanned forwards and not backwards. With these factors in 
mind, I am implementing the following technique. I hope the community 
can point out flaws and provide improvements.

While the pages are not deterministic, we can still pre-compute them to 
provide a best-guess and have some semblance of a normal page interaction:

 Normal Page Interaction
 -----------------------
 | Beginning | Previous | Jump to Page | Next | Last Page
 |    <<     |    <     | 1  2  ...  n |  >   |   >>

There is an obvious problem with this technique. What if 100 rows were added 
between pages one and two but the page size was 50? Some of the records 
would never be displayed or discoverable.

Before suggesting a refinement to address this issue, let me talk about the 
pagination pre-computation using the TedgeField (from my extension to D4M) 
as an example:

    scanner = connector.createScanner(tableName, new Authorizations());
    Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
    while (iterator.hasNext()) {
        Entry<Key, Value> entry = iterator.next();
        String fieldName = entry.getKey().getRow();
        if (entryCount == 0 || (entryCount % pageSize == 0)) {
            System.out.println(String.format("%d,%s", pageNumber, fieldName));
            pageNumber++;
        }
        entryCount++;
    }

The TedgeField table looks like this:

   ROW          * CQ      * VALUE
 ---------------------------------
   STATE_NAME   * field   * 1
   CITY_NAME    * field   * 1    

After the pre-compute is run, you might see:

1,a00100
2,a02500
3,a10300
4,a59660
5,n01400
6,n07220
7,n18450
8,state

When the user clicks a page number (let's say 3, an appropriate scanner is 
initialized.

        scan.setBatchSize(batchSize);
        scan.setRange(new Range(new Text("a10300", true, null, true);

For a page size of 5 you might see the following:

a10300_01
a10300_02
a10300_03
a10300_04
a10300_05

This looks just fine. But notice that page 4 starts with a59660. And there 
could be a lots of entries between a10300_05 and a59660 that were added 
since the last pre-computation of pagination. So I am changing the page
interaction elements to be:

 Suggested Page Interaction
 --------------------------
 | Beginning | Previous | Jump to Page | Next  | Last Page
 |    <<     |    <     | 1  2  ...  n |  >?   |   >>?

I think the Normal Page Interaction needs to be modified. The new meaning is 
that clicking next steps through every entry. Pages become elastic - you only 
get to the next page number when the top row of the page (as determined by 
precomputing) is passed. Thus the addition of the Question Mark to the Next 
and Last Page links. In contrast, the Previous function remains the same. You 
jump immediately to the row associated with the page or to the next row 
visible to you.

The timestamp of the search results can be compared to the timestamp of the
pre-computed page breaks so that a 'NEW SINCE PRE-COMPUTE' indicator can be 
added to the search results. On the other hand, users may not care.

See the FieldPaginationPrecomputor for an implementation of pre-computing
page breaks.

I'm tempted to suggest that the jump values be encrypted so they are opaque 
to users and no information is leaked.

Any suggestions or improvments?
