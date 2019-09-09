package models

import org.mongodb.scala.bson.ObjectId

/**
  * Coupon Class
  * name:String = name of the coupon
  * description:String = description of the coupon
  * picPath:String = the path to the pic of the coupon
  * adminId:ObjectId = the id of the admin this coupon belongs to
  */
object Coupon {
  def apply(name:String, description:String, picPath:String, adminId:ObjectId): Coupon =
    Coupon(new ObjectId(), name, description, picPath, adminId)
}

case class Coupon(val _id:ObjectId, var name:String, var description:String, var picPath:String, var adminId:ObjectId) {
  // constructor
  println("New coupon has just been created!")

  // getters and setters
  def getId():ObjectId = this._id
  def setName(newName:String):Unit = this.name = newName
  def getName():String = this.name
  def setDescription(newDescription:String):Unit = this.description = newDescription
  def getDescription():String = this.description
  def setPicPath(newPicPath:String):Unit = this.picPath = newPicPath
  def getPicPath():String = this.picPath
  def setAdminId(newAdminId:ObjectId):Unit = this.adminId = newAdminId
  def getAdminId():ObjectId = this.adminId
}
