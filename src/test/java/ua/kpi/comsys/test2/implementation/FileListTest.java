/*
 * Copyright (c) 2014-2015, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class FileListTest {


    File f;
    static String PREFIX = "src/test/resources/";
    static String FILE_1 = PREFIX+"1.txt";
    static String FILE_14 = PREFIX+"14.txt";
    static String FILE_227 = PREFIX+"227.txt";
    static String FILE_BIG = PREFIX+"BigNumber.txt";
    static String FILE_EMPTY = PREFIX+"EmptyFile.txt";


    @After
    public void tearDown() {
        f = null;
    }

    @Test
    public void testFileExists() throws IOException {
        f = new File(FILE_1);
        assertNotNull(f);
        assertTrue("File does not exists!", f.exists());

        BufferedReader br = new BufferedReader(new FileReader(f));
        String str = br.readLine();
        assertNull("File 1.txt is corrupted ", br.readLine());
        int i = Integer.parseInt(str);

        assertEquals("File 1.txt is corrupted ", 1, i);

        br.close();

    }

    @Test
    public void testReadSingleDigit() {
        f = new File(FILE_1);
        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertEquals("Incorrect list loading", 1, list.size());
    }

    @Test
    public void testIncorrectFileName() {
        f = new File(PREFIX,"sadfdsfwf");
        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertTrue("When file name is incorrect, list should be empty", list.isEmpty());
    }

    @Test
    public void testEmptyFile() {
        f = new File(FILE_EMPTY);
        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertTrue("When file name is incorrect, list should be empty", list.isEmpty());
    }

    @Test
    public void testWriteSingleDigit() throws IOException {
        f = new File(FILE_1);
        assertNotNull(f);
        assertTrue("File does not exists!", f.exists());

        BufferedReader br = new BufferedReader(new FileReader(f));
        String str = br.readLine();
        br.close();

        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertFalse("Failed to load list from file", list.isEmpty());
        assertEquals("Incorrect list loading", 1, list.size());


        list.saveList(f);

        br = new BufferedReader(new FileReader(f));
        String newStr = br.readLine();
        assertNull("File 1.txt is corrupted ", br.readLine());
        assertEquals("Incorrect write to file", str, newStr);

    }

    @Test
    public void testWriteLongList() throws IOException {
        f = new File(FILE_BIG);
        assertNotNull(f);
        assertTrue("File does not exists!", f.exists());

        BufferedReader br = new BufferedReader(new FileReader(f));
        String str = br.readLine();
        br.close();

        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertFalse("Failed to load list from file", list.isEmpty());


        list.saveList(f);

        br = new BufferedReader(new FileReader(f));
        String newStr = br.readLine();
        assertNull("File 1.txt is corrupted ", br.readLine());
        assertEquals("Incorrect write to file", str, newStr);

        NumberListImpl list2 = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list2);
        assertFalse("Failed to load list from file", list2.isEmpty());

        assertEquals("Lists are not equal after save/restore operation",list, list2);

    }


    @Test
    public void testToString() throws IOException {
        f = new File(FILE_BIG);
        assertNotNull(f);
        assertTrue("File does not exists!", f.exists());

        BufferedReader br = new BufferedReader(new FileReader(f));
        String str = br.readLine();
        br.close();

        NumberListImpl list = new NumberListImpl(f);
        assertNotNull("Failed to load list from file", list);
        assertFalse("Failed to load list from file", list.isEmpty());

        assertEquals(str, list.toDecimalString());
    }
}
