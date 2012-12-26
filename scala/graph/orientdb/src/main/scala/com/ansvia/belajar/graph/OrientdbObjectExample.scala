package com.ansvia.belajar.graph

import com.orientechnologies.orient.core.id.ORecordId
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import models.User
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import com.orientechnologies.orient.core.db.`object`.ODatabaseObject
import com.orientechnologies.orient.`object`.db.OObjectDatabaseTx

object OrientdbObjectExample {


    implicit def dbWrapper(db: OObjectDatabaseTx) = new {
        def queryBySql[T](sql: String, params: AnyRef*): List[T] = {
            val params4java = params.toArray
            val results: java.util.List[T] = db.query(new OSQLSynchQuery[T](sql), params4java: _*)
            for ( rv <- results.asScala.toList )
            yield {
                db.detach(rv)
                rv
            }
        }
    }

    def timing(title:String = "title")(f: => Unit){
        println(title + ":")

        val start = System.currentTimeMillis()

        f

        val totalTime = System.currentTimeMillis() - start
        println(title + " - done in " + totalTime + "ms")
    }

    def main(args:Array[String]){

        val uri = "local:/tmp/orientdb"
//        val uri = "memory:OrientdbObjectExample"
//        val orient = Orient.instance()
        val dbx = new OObjectDatabaseTx(uri)

        val db:OObjectDatabaseTx =
            if (dbx.exists())
                dbx.open("admin", "admin")
            else{
                dbx.create()
            }

//        db.getMetadata.getSchema.createClass(classOf[User])
        db.getEntityManager.registerEntityClass(classOf[User])
//        db.getMetadata.reload()

        val gondez = new User()
        gondez.name = "gondez"

        val robin = new User()
        robin.name = "robin"
        robin.supports.add(gondez)

        db.detach(robin)
        db.save(robin)

        println("count class elements: " + db.countClusterElements("User"))


        var result: List[User] = db.queryBySql[User]("select * from User")

        timing("get from sql like query"){
            for ( u <- result ){
    //            db.detach(u)
                println(" + " + u)
            }
        }

        timing("Get from class browser"){

            val browser = db.browseClass(classOf[User])

            val it = browser.iterator()
            while (it.hasNext){
                val u = it.next()
                db.detach(u)
                println(" + " + u)
            }

        }

//        println("search for gondez and support: ")

        timing("Search for gondez and support"){
            result=db.queryBySql[User]("select * from User where supports contains (name='gondez')")

            for (u <- result){
                println(" + " + u)
            }
        }


        db.drop()
        db.close()

        //      val oclass = db.getMetadata.getSchema.createClass(classOf[User])
        //
        //      val user1 = db.save(new User())


    }
}

