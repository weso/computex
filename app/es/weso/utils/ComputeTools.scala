package es.weso.utils

object ComputeTools {

  def computeDebug[A](steps: Seq[A => A], initial: A, merge: (A,A) => A) : (A,Seq[A]) = {
    steps.foldLeft((initial,Seq[A]()))(combineDebug(merge))
  }
  
  def combineDebug[A](merge:(A,A)=>A)(state:(A,Seq[A]), step: A => A): (A,Seq[A]) = {
    val computed = step(state._1)
    val merged = merge(state._1,computed)
    (merged,state._2 :+ computed)
  }
  
  def compute[A](steps: Seq[A => A], initial: A, merge:(A,A) => A) : A = {
    steps.foldLeft(initial)(combine(merge))
  }
  
  def combine[A](merge:(A,A)=>A)(state: A, step: A => A) : A = {
    val computed = step(state)
    merge(state,computed)
  }
  
}