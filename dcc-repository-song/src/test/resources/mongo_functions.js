// Run mongo after running the SONG extraction, and loading the production 
// PCAWGFile and CollabFile into the same collection ('dcc-repository)
// 
// Copy this file into your current directory on the host that you're running the mongo client
// on. You only need to run createIndexes() if they haven't already been created for all three
// collections; you can use db.<collection>.showIndexes() to find out.
// 
// From the mongo prompt, type:
// use dcc-repository
// load("mongo_functions.js")
// createIndexes()  
// createStatusCollection() 
// summarizeErrors()

function createIndexes() {
    db.SONG.createIndex({"object_id": 1})
    db.PCAWGFile.createIndex({"object_id": 1})
    db.CollabFile.createIndex({"object_id": 1})
}

function same(x, y) {
    if (x == undefined && y == undefined) {
        return true;
    }

    if (x == undefined && y != undefined) {
        return false;
    }
    if (x != undefined && y == undefined) {
        return false;
    }

    if (typeof(x) == "function" || typeof(y) == "function") {
        return true;
    }
    if (typeof(x) == "number") {
        return x == y;
    }

    if (typeof(x) == "string") {
        return x == y;
    }

    if (x instanceof Array) {
        if (x.length != y.length) {
            return false;
        }
        for (var i = 0; i < x.length; i++) {
            if (!same(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    assert(typeof(x) == "object", "unknown data type " + typeof(x) + "for object" + x)

    for (key in x) {
        if (!same(x[key], y[key])) {
            return false;
        }
    }
    return true;
}

function check(o1, o2, fields) {
    function identical(field) {
        return same(o1[field], o2[field]);
    }

    function bad(field) {
        return !identical(field);
    }

    if (!fields.every(identical)) {
        return {"ok": false, "id": o1.object_id, "bad": fields.filter(bad)};

    }

    return {"id": o1.object_id, "ok": true, "bad": []};
}

function eq(x) {
    return function (y) {
        return x == y
    }
}

function drop(blacklist) {
    return function (x) {
        return !blacklist.some(eq(x))
    }
}

function compare_objects(test, good, blacklist) {
    var keys = Object.keys(good).filter(drop(blacklist))
    return check(test, good, keys)
}

function compare_array(song, good, field_name, ignore_list) {
    var status = {"ok": true, "bad": []}
    var a1 = song[field_name]
    var a2 = good[field_name]

    if (a1.length != a2.length) {
        status.ok = false;
        status.bad.push(field_name + "(length)");
        return status;
    }

    for (i in a1) {
        item_status = compare_objects(a1[i], a2[i], ignore_list)
        if (!item_status.ok) {
            status.ok = false;
            status.bad.push(field_name + "[" + i + "]: " + item_status.bad)
        }
    }
    return status;
}

function compare(song, good) {
    if (good == undefined) {
        return {"id": song.object_id, "ok": false, bad: ["no match"]}
    }

    var main_ignore_fields = ["_id", "id", "file_copies", "donors"];
    // The id fields won't match, and we compare the file_copies and donors arrays 
    // separately later on (they have fields for us to ignore, too)

    var file_ignore_fields = ["last_modified", "repo_org", "repo_code", "repo_metadata_path"];
    // we don't have the last_modified date; we should set it to the current date
    // the repo_org and repo_code for SONG is song...
    // song doesn't have a metadata path

    var donor_ignore_fields = ["donor_id", "specimen_id", "sample_id"]
    // These id fields will will vary between current PCAWG/Collaboratory and SONG

    var status = compare_objects(song, good, main_ignore_fields);
    var file_status = compare_array(song, good, 'file_copies', file_ignore_fields);
    if (!file_status.ok) {
        status.ok = false;
        status.bad.push("file_status:" + file_status.bad)
    }

    var donor_status = compare_array(song, good, 'donors', donor_ignore_fields);
    if (!donor_status.ok) {
        status.ok = false;
        status.bad.push("donor_status:" + donor_status.bad);
    }

    return status;
}

function check_document(o) {
    var y = db.PCAWGFile.find({object_id: o.id});
    var z = db.CollabFile.find({object_id: o.id});
    var keep = y.toArray()[0];
    var collab = z.toArray()[0];
    if (keep != undefined && collab != undefined) {
        keep.file_copies = collab.file_copies;
    } else if (keep != undefined && collab == undefined) {
        keep.file_copies = [];
    }

    return compare(o, keep);
}

function map_documents() {
    db.SONG.find().map(check_document).forEach(function (d) {
        db.status.insert(d)
    })
}

function createStatusCollection() {
    db.status.drop();
    db.createCollection("status");
    map_documents();
}

function countErrors(errorCode) {
    var o = {}
    o[errorCode] = db.status.find({"bad": errorCode}).count()
    return o;
}

function summarizeErrors() {
    return db.status.distinct("bad").map(countErrors);
}

