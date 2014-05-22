
# Field Cardinality in D4M

## Cardinality in Existing Data

>Cardinality is the number of unique values in a field. Also known as the Domain.

The D4M schema tracks field values in the D4M TedgeDegree table. For example:

```
-------------------------------------------
      ROW            / CF / CQ     / Value
---------------------/----/--------/-------
census2010pop|101547 /    / degree / 1
census2010pop|1018   /    / degree / 2
census2010pop|10183  /    / degree / 2
```

This information tells us how many entries have the value of 1018 in the 
_census2010pop_ field (two of them). However, the number of unique 
_census2010pop_ values is not stored. For this table, finding the answer of
three is trival. When the data field has more variability (say, last_name) 
the cardinality can take significantly more effort to determine.

In this note, we'll first show to how calculate cardinality and store the
information into the TedgeMetadata table.

>http://static.googleusercontent.com/media/research.google.com/en/us/pubs/archive/40671.pdf

Other sources describe the underlying technique this code uses so I
won't go into any detail. The code below is taken from
FieldCardinalityUsingHyperLogLogPlus.java. See the code comments to understand
what is being done.

```
        Map<String, ICardinality> estimators = new TreeMap<String, ICardinality>();

        Scanner scan = connector.createScanner(tableName, new Authorizations());
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();

            // ROW format: census2010pop|10031 :degree []    1
            String row = entry.getKey().getRow().toString();

            // factName format: census2010pop
            String factName = row.substring(0, row.indexOf(factDelimiter));

            // Turn the combiniation of fact and value into a hash.
            long hashCode = MurmurHash.hash64(row);

            // Create a Cardinality Estimator for each fact.
            ICardinality estimator = estimators.get(factName);
            if (estimator == null) {
                estimator = new HyperLogLogPlus(16);
                estimators.put(factName, estimator);
            }

            // Add the fact and value into the estimator.
            estimator.offer(hashCode);
        }

        //Writing this entry format:
        //field cardinality:census2010pop [] 640
        Text field = new Text("field");
        Text cardinality = new Text("cardinality");
        Mutation mutation = new Mutation(field);
        for (Entry<String, ICardinality> entry : estimators.entrySet()) {
            Text factName = new Text(entry.getKey());
            ICardinality estimator = entry.getValue();
            Value cardinalityEstimate = new Value(Long.toString(estimator.cardinality()).getBytes(charset));
            mutation.put(cardinality, factName, cardinalityEstimate);
        }
        
        // New cardinality values overwrite the old values.
        BatchWriter writer = connector.createBatchWriter(tableManager.getMetadataTable(), 10000000, 10000, 5);
        writer.addMutation(mutation);
        writer.close();
```

After this code is run, your TedgeMetadata table might look like this:

```
> grep county
---------------------------------------
  ROW   / CF          / CQ     / Value
--------/-------------/--------/-------
field   / cardinality / county / 68
field   / field       / county / 1104
```
You can easily see that the county field can be reasonably be used in a 
drop-down field for a user interface. Or pulled entirely into memory.

## Cardinality During Ingest

>In order to run these examples I bumped the tserver max ram to 512M instead of 128M.

Tracking cardinality during ingest is harder than using the 
on-the-fly technique shown above. We'll need a place to store the cardinality
values and a place to store the information (the byte array) used by the
cardinality estimator. 

The MutationFactory.generateMetadata method returns a List of Mutations 
containing the entry counts, cardinalities, and cardinality byte arrays. 
All of the information is mashed together because the project is only 
for prototypes. As such, the information is stored in the TedgeMetadata
table using the following schema:

```
-------------------------------------------
  ROW /  CF               / CQ     / Value
--------------------------/--------/-------
field / cardinality       / county / 68
field / cardinality       / state  / 1
field / cardinality_bytes / county / \xFF\xFF\xFF\xFE\x10\x00\x00\xAC...
field / cardinality_bytes / state  / \xFF\xFF\xFF\xFE\x10\xD5\x02\x00...
```

>Only the first eight bytes of the backing byte array are shown above.

You can evolve a more sophisticated codebase by merging the cardinality
byte array coming out of the getMetadata table with the information already
in the TedgeMetadata table. Contact me if you need more guidance.

>StateCSVToAccumulo.java is setup to track Cardinality during ingest.
