package workflow

import org.apache.spark.rdd.RDD

/**
 * This is the result of fitting a [[Pipeline]]. It is logically equivalent to the Pipeline it is produced by,
 * but with all Estimators pre-fit, and only containing Transformers in the underlying graph.
 * Applying a FittedPipeline to new data does not trigger any new optimization or estimator fitting.
 *
 * Unlike normal Pipelines, FittedPipelines are serializable and may be written to and from disk.
 *
 * @param transformerGraph The DAG representing the execution (only contains Transformers)
 * @param source The SourceId of the Pipeline
 * @param sink The SinkId of the Pipeline
 * @tparam A type of the data this FittedPipeline expects as input
 * @tparam B type of the data this FittedPipeline outputs
 */
class FittedPipeline[A, B] private[workflow] (
    private[workflow] val transformerGraph: TransformerGraph,
    private[workflow] val source: SourceId,
    private[workflow] val sink: SinkId
  ) extends Chainable[A, B] with Serializable {

  /**
   * Converts this FittedPipeline back into a Pipeline.
   */
  override def toPipeline: Pipeline[A, B] = new Pipeline(
    new GraphExecutor(transformerGraph.toGraph, optimize = false),
    source,
    sink)

  /**
   * The application of this FittedPipeline to a single input item.
   *
   * @param in  The input item to pass into this transformer
   * @return  The output value
   */
  def apply(in: A): B = toPipeline.apply(in).get()

  /**
   * The application of this FittedPipeline to an RDD of input items.
   *
   * @param in The RDD input to pass into this transformer
   * @return The RDD output for the given input
   */
  def apply(in: RDD[A]): RDD[B] = toPipeline.apply(in).get()

}
