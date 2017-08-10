package fire.nodes.examples;

import fire.context.JobContext;
import fire.output.Output;
import fire.output.OutputTable;
import fire.workflowengine.Node;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * Created by jayantshekhar on 5/15/16.
 */
public class NodeTestPrintFirstNRows extends Node {


    public int n = 5;

    public NodeTestPrintFirstNRows() {}

    public NodeTestPrintFirstNRows(int i, String nm, int tempn) {
        super(i, nm);

        n = tempn;

    }

    @Override
    public void execute(JobContext jobContext) throws Exception {

        // get the first n rows
        Row[] rows = (Row[])dataFrame.take(n);
        if (rows == null || rows.length == 0)
            return;

        int numRows = rows.length;
        int numCols = rows[0].length();

        // create result 2d array
        String[][] values = new String[numRows+2][numCols];

        // fill in the schema
        StructType structType = dataFrame.schema();
        StructField[] fields = structType.fields();
        for (int i=0; i<fields.length; i++) {
            values[0][i] = fields[i].name();
            values[1][i] = fields[i].dataType().toString();
        }

        // fill in the values
        for (int i=0; i<numRows; i++) {
            Row row = rows[i];
            for (int j=0; j<numCols; j++) {
                if (row.get(j) == null)
                    values[i+2][j] = "";
                else
                    values[i+2][j] = row.get(j).toString();
            }
        }

        // create output table
        OutputTable outputTable = new OutputTable();
        outputTable.id = id;
        outputTable.name = name;
        outputTable.title = "Row Values";
        outputTable.cellValues = values;
        outputTable.resultType = Output.RESULTTYPE_DATA;

        // output to workflow context
        jobContext.workflowctx().outTable(this, outputTable);

        passDataFrameToNextNodesAndExecute(jobContext, dataFrame);
    }

}
