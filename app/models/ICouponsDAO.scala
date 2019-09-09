package models

import org.mongodb.scala.bson.ObjectId

trait ICouponsDAO {
  // admin functions
  /**
    * Function that adds a new admin user to the DB
    * @param userName:String = the username of the new admin
    * @param password:String = the password of the new admin
    * @param companyName:String = the company name of the new admin
    */
  def addAdmin(userName:String, password:String, companyName:String):Unit
  /**
    * Function that updates an admin's password
    * @param userName:String = the username of the admin
    * @param newPassword:String = the new password
    */
  def changeAdminPassword(userName:String, newPassword:String):Unit

  /**
    * Function that returns an admin's id
    * @param userName:String = username of the admin
    * @return ObjectId = the id of the admin in the DB
    */
  def getAdminId(userName:String):ObjectId

  /**
    * Function that checks if an admin exists in the DB
    * @param userName:String = username of the admin
    * @param password:String = password of the admin
    * @return Boolean = True if the admin exists, False otherwise
    */
  def isAdminExist(userName:String, password:String):Boolean

  /**
    * Function that returns a list of all the admin's coupons
    * @param adminId:ObjectId = the admin's id
    * @return List[Coupon] = a list of coupons
    */
  def getAdminCouponsList(adminId:ObjectId):List[Coupon]

  /**
    * Function that returns an instance of the admin user
    * @param adminId:ObjectId = the admin's id in the DB
    * @return Admin = instance of Admin class
    */
  def getAdminAsInstance(adminId:ObjectId):Admin

  // coupon functions
  /**
    * Function that adds a new coupon to the DB
    * @param name:String = name of the new coupon
    * @param description:String = description of the new coupon
    * @param picPath:String = the path of the pic on the server
    * @param adminId:ObjectId = the admin id that this new coupon belongs to
    */
  def addCoupon(name:String, description:String, picPath:String, adminId:ObjectId):Unit

  /**
    * Function that deletes a coupon from the DB
    * @param id:ObjectId = the id of the coupon to be deleted
    */
  def delCoupon(id:ObjectId):Unit

  /**
    * Function that returns a coupon's id
    * @param name:String = the name of the coupon
    * @param adminId:ObjectId = the id of the admin this coupon associated with
    * @return ObjectId = the id of the found coupon
    */
  def getCouponId(name:String, adminId:ObjectId):ObjectId

  /**
    * Function that modifies a coupon's name
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newName:String = the new name for the coupon
    */
  def modifyCouponName(oldCouponId:ObjectId, newName:String):Unit

  /**
    * Function that modifies a coupon's description
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newDescription:String = the new description for the coupon
    */
  def modifyDescriptionName(oldCouponId:ObjectId, newDescription:String):Unit

  /**
    * Function that modifies a coupon's pic path
    * @param oldCouponId:ObjectId = the id of the coupon to be updated
    * @param newPathName:String = the new pic path for the coupon
    */
  def modifyPicPathName(oldCouponId:ObjectId, newPathName:String):Unit

  /**
    * Function that returns all the coupons in the DB
    * @return List[Coupon] = list of all the coupons
    */
  def getAllCoupons():List[Coupon]
}
