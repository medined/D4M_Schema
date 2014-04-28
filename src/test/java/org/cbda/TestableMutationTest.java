package org.cbda;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestableMutationTest {

    Text row = new Text("b");
    Text cf = new Text("cf01");
    Text cq = new Text("cq01");
    Value value = new Value("value".getBytes());

    @Test
    public void test_different_rows() {
        TestableMutation expected = new TestableMutation("b");
        Mutation actual = new Mutation("a");
        assertNotEquals(expected, actual);
    }

    @Test
    public void test_different_entry_count() {
        TestableMutation expected = new TestableMutation(row);
        expected.put(cf, cq, value);
        expected.put(new Text("cf02"), cq, value);

        Mutation actual = new Mutation(row);
        actual.put(cf, cq, value);

        assertNotEquals(expected, actual);
    }

    @Test
    public void test_different_cf() {
        TestableMutation expected = new TestableMutation(row);
        expected.put(cf, cq, value);

        Mutation actual = new Mutation(row);
        actual.put(new Text("cf02"), cq, value);

        assertNotEquals(expected, actual);
}

    @Test
    public void test_different_cq() {
        TestableMutation expected = new TestableMutation(row);
        expected.put(cf, cq, value);

        Mutation actual = new Mutation(row);
        actual.put(cf, new Text("cq02"), value);

        assertNotEquals(expected, actual);
    }

    @Test
    public void test_different_value() {
        TestableMutation expected = new TestableMutation(row);
        expected.put(cf, cq, value);

        Mutation actual = new Mutation(row);
        actual.put(cf, cq, new Value("not_expected.".getBytes()));

        assertNotEquals(expected, actual);
    }

}
