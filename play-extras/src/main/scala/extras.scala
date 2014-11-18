package com.gravitydev.playextras

import play.api.mvc._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, LocalDate}

class MappedPathBindable[T, B : PathBindable](to: T => B, from: B => Either[String,T]) extends PathBindable[T] {
  val binder = implicitly[PathBindable[B]]

  override def bind (key: String, value: String): Either[String,T] = for {
    v <- binder.bind(key,value).right
    mId <- from(v).right
  } yield mId

  override def unbind (key: String, value: T): String = binder.unbind(key, to(value)) 
}

class MappedQueryStringBindable[T, B: QueryStringBindable](to: T => B, from: B => Either[String,T]) extends QueryStringBindable[T] {
  val binder = implicitly[QueryStringBindable[B]]

  override def bind (key: String, params: Map[String, Seq[String]]): Option[Either[String,T]] = {
    binder.bind(key, params) map {res =>
      for {
        v <- res.right
        mId <- from(v).right
      } yield mId
    }
  }

  override def unbind (key: String, value: T): String = binder.unbind(key, to(value)) 
}

trait Binders {
  type Bindable[X]

  def wrap [T, B : Bindable](to: T => B, from: B => Either[String,T]): Bindable[T]

  implicit def stringBindable: Bindable[String]

  def jodaDateTime (formatPattern: String = "yyyy-MM-dd'T'HH:mm:ssZZ"): Bindable[DateTime] = {
    val format = DateTimeFormat.forPattern(formatPattern)
    wrap[DateTime,String](
      dateTime => format.print(dateTime),
      dateTimeStr => try Right(format.parseDateTime(dateTimeStr)) catch {case e: Exception => Left(e.getMessage)}
    )
  }

  def jodaLocalDate (formatPattern: String = "yyyy-MM-dd"): Bindable[LocalDate] = {
    val format = DateTimeFormat.forPattern(formatPattern)
    wrap[LocalDate,String](
      localDate => format.print(localDate),
      localDateStr => try Right(format.parseLocalDate(localDateStr)) catch {case e: Exception => Left(e.getMessage)}
    )
  }

}

object QueryStringBinders extends Binders { 
  type Bindable[X] = QueryStringBindable[X]
  def wrap [T, B : QueryStringBindable](to: T => B, from: B => Either[String,T]): QueryStringBindable[T] = new MappedQueryStringBindable(to,from)
  def stringBindable = implicitly[QueryStringBindable[String]]
}

object PathBinders {
  type Bindable[X] = PathBindable[X]
  def wrap [T, B : PathBindable](to: T => B, from: B => Either[String,T]): PathBindable[T] = new MappedPathBindable(to,from)
  def stringBindable = implicitly[PathBindable[String]]
}

