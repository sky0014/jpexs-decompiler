package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFBundle;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.ReReadableInputStream;
import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggySwfBundle implements SWFBundle {

    private IggyFile iggyFile;

    public IggySwfBundle(InputStream is) throws IOException {
        this(is, null);
    }

    public IggySwfBundle(File filename) throws IOException {
        this(null, filename);
    }

    protected IggySwfBundle(InputStream is, File filename) throws IOException {
        initBundle(is, filename);
    }

    protected void initBundle(InputStream is, File filename) throws IOException {
        if (filename == null) {
            filename = File.createTempFile("bundle", ".iggy");
            Helper.saveStream(is, filename);
        }
        iggyFile = new IggyFile(filename);
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Set<String> getKeys() {
        Set<String> ret = new TreeSet<>();
        for (int i = 0; i < length(); i++) {
            ret.add(iggyFile.getSwfName());
        }
        return ret;
    }

    private int keyToSwfIndex(String key) {
        for (int i = 0; i < length(); i++) {
            if (key.equals(iggyFile.getSwfName())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Key " + key + " does not exist!");
    }

    @Override
    public SeekableInputStream getSWF(String key) throws IOException {
        SWF swf = IggyToSwfConvertor.getSwf(iggyFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        swf.saveTo(baos);
        MemoryInputStream mis = new MemoryInputStream(baos.toByteArray());
        return mis;
    }

    @Override
    public Map<String, SeekableInputStream> getAll() throws IOException {
        Map<String, SeekableInputStream> ret = new HashMap<>();
        for (String key : getKeys()) {
            ret.put(key, getSWF(key));
        }
        return ret;
    }

    @Override
    public String getExtension() {
        return "iggy";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean putSWF(String key, InputStream is) throws IOException {
        try {
            SWF swf = new SWF(is, false, false);
            SwfToIggyConvertor.updateIggy(iggyFile.getSwf(), swf);
            iggyFile.saveChanges();
            return true;
        } catch (InterruptedException ex) {
            return false;
        }

    }

}
