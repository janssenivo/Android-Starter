package com.kinvey.bookshelf.veryverylongname.toseeif.evenmore.realmstufftesting.another.level.ofindirection.wecanmesswithrealm;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;


public class Book extends GenericJson {

    @Key("name")
    public String name;

    @Key("image_id")
    public String imageId;

    @Key("nestedstuff")
    public NestedBook nestedBook;

}

