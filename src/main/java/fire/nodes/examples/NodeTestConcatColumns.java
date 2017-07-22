package fire.nodes.examples;

import fire.context.JobContext;
import fire.nodes.etl.NodeETL;
import fire.workflowengine.FireSchema;
import fire.workflowengine.Workflow;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset; import org.apache.spark.sql.Row;
import scala.collection.mutable.ListBuffer;

import static org.apache.spark.sql.functions.*;

import java.io.Serializable;

public class NodeTestConcatColumns extends NodeETL implements Serializable {
    public String[] inputCols;
    public String outputCol;
    public String sep = "|";

    public NodeTestConcatColumns(){

    }

    public NodeTestConcatColumns(int i, String name, String inputcolumns, String outputCol, String sep){
        super(i, name);

        this.inputCols = inputcolumns.trim().split(" ");
        this.outputCol = outputCol;
        this.sep = sep;
    }

    @Override
    public void execute(JobContext jobContext) throws Exception {

        ListBuffer<Column> list = new ListBuffer<>();

        for (String s : inputCols) {
            Column col = new Column(s);
            list.$plus$eq(col);
        }

        Dataset<Row> out=  dataFrame.select(col("*"),concat_ws(sep, list).as(outputCol));

        passDataFrameToNextNodesAndExecute(jobContext, out);

    }

    @Override
    public FireSchema getOutputSchema(Workflow workflow, FireSchema inputSchema) {

        inputSchema.addColumn(outputCol, FireSchema.Type.STRING, FireSchema.MLType.TEXT);

        return inputSchema;
    }

}
