package com.kinvey.bookshelf.veryverylongname.toseeif.evenmore.realmstufftesting.another.level.ofindirection.wecanmesswithrealm;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by ivo on 5/15/17.
 */

public class NestedBook extends GenericJson{

    @Key("name")
    public String name;

    @Key("foo")
    public String foo;

}
