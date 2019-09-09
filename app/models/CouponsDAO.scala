package models

import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import models.MongoHelpers._

object CouponsDAO extends ICouponsDAO {
  // constructor, creating the connecting to mongoDB
  // the connection to the mongodb client
  val mongoClient:MongoClient = MongoClient()
  // codec for the classes
  val codecRegistry = fromRegistries(fromProviders(classOf[Coupon], classOf[Admin]), DEFAULT_CODEC_REGISTRY )
  // database instance
  val database: MongoDatabase = mongoClient.getDatabase("projectDB").withCodecRegistry(codecRegistry)
  // coupons collection instance
  val couponsCO: MongoCollection[Coupon] = database.getCollection("coupons")
  // admins collection instance
  val adminsCO: MongoCollection[Admin] = database.getCollection("admins")
  println("DB connection is opened.")

  // admin functions
  /**
    * Function that adds a new admin user to the DB
    * @param userName:String = the username of the new admin
    * @param password:String = the password of the new admin
    * @param companyName:String = the company name of the new admin
    */
  override def addAdmin(userName:String, password:String, companyName:String):Unit={
    val newAdmin:Admin = Admin(userName,password, companyName)
    adminsCO.insertOne(newAdmin).results()
  }

  /**
    * Function that updates an admin's password
    * @param userName:String = the username of the admin
    * @param newPassword:String = the new password
    */
  override def changeAdminPassword(userName:String, newPassword:String):Unit={
    adminsCO.updateOne(equal("userName", userName), set("password", newPassword)).printHeadResult("Update Result: ")
  }

  /**
    * Function that returns an admin's id
    * @param userName:String = username of the admin
    * @return ObjectId = the id of the admin in the DB
    */
  override def getAdminId(userName:String):ObjectId={
    adminsCO.find(equal("userName", userName)).first.results().head._id
  }

  /**
    * Function that checks if an admin exists in the DB
    * @param userName:String = username of the admin
    * @param password:String = password of the admin
    * @return Boolean = True if the admin exists, False otherwise
    */
  override def isAdminExist(userName:String, password:String):Boolean={
    if (userName.isEmpty || password.isEmpty)
      return false

    val found = adminsCO.find(equal("userName", userName)).first.results()

    if (found.isEmpty)
      return false
    else if (found.head.getPassword() != password){
      println("Wrong password")
      return false
    }
    true
  }

  /**
    * Function that returns a list of all the admin's coupons
    * @param adminId:ObjectId = the admin's id
    * @return List[Coupon] = a list of coupons
    */
  override def getAdminCouponsList(adminId:ObjectId):List[Coupon]={
    val coupons:List[Coupon] = couponsCO.find(equal("adminId", adminId)).results().toList

    if (coupons.isEmpty)
      return null

    coupons
  }

  /**
    * Function that returns an instance of the admin user
    * @param adminId:ObjectId = the admin's id in the DB
    * @return Admin = instance of Admin class
    */
  override def getAdminAsInstance(adminId:ObjectId):Admin={
    adminsCO.find(equal("_id", adminId)).first.results().head
  }

  // coupon functions
  /**
    * Function that adds a new coupon to the DB
    * @param name:String = name of the new coupon
    * @param description:String = description of the new coupon
    * @param picPath:String = the path of the pic on the server
    * @param adminId:ObjectId = the admin id that this new coupon belongs to
    */
  override def addCoupon(name:String, description:String, picPath:String, adminId:ObjectId):Unit={
    val newCoupon:Coupon = Coupon(name, description, picPath, adminId)
    couponsCO.insertOne(newCoupon).results()
  }

  /**
    * Function that deletes a coupon from the DB
    * @param id:ObjectId = the id of the coupon to be deleted
    */
  override def delCoupon(id:ObjectId):Unit={
    couponsCO.deleteOne(equal("_id", id)).printHeadResult("Delete Result: ")
  }

  /**
    * Function that returns a coupon's id
    * @param name:String = the name of the coupon
    * @param adminId:ObjectId = the id of the admin this coupon associated with
    * @return ObjectId = the id of the found coupon
    */
  override def getCouponId(name:String, adminId:ObjectId):ObjectId={
    val couponsList = getAdminCouponsList(adminId)

    if (couponsList.isEmpty)
      return null

    for (coupon <- couponsList)
      if (coupon.getName().equals(name))
        return coupon.getId()

    null
  }

  /**
    * Function that modifies a coupon's name
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newName:String = the new name for the coupon
    */
  override def modifyCouponName(oldCouponId:ObjectId, newName:String):Unit={
    couponsCO.updateOne(equal("_id", oldCouponId), set("name", newName)).printHeadResult("Update Result: ")
  }

  /**
    * Function that modifies a coupon's description
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newDescription:String = the new description for the coupon
    */
  override def modifyDescriptionName(oldCouponId:ObjectId, newDescription:String):Unit={
    couponsCO.updateOne(equal("_id", oldCouponId), set("description", newDescription)).printHeadResult("Update Result: ")
  }

  /**
    * Function that modifies a coupon's pic path
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newPathName:String = the new pic path for the coupon
    */
  override def modifyPicPathName(oldCouponId:ObjectId, newPathName:String):Unit={
    couponsCO.updateOne(equal("_id", oldCouponId), set("picPath", newPathName)).printHeadResult("Update Result: ")
  }

  /**
    * Function that returns all the coupons in the DB
    * @return List[Coupon] = list of all the coupons
    */
  override def getAllCoupons():List[Coupon]={
    couponsCO.find().results().toList
  }
}