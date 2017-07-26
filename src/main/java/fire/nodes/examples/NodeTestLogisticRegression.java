/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fire.nodes.examples;

import fire.context.JobContext;
import fire.nodes.ml.NodePredictor;
import fire.schemautil.SchemaUtil;
import fire.util.parse.ParseDouble;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;

import java.io.Serializable;

/**
 * Created by jayantshekhar
 */
public class NodeTestLogisticRegression extends NodePredictor implements Serializable {

    public String rawPredictionCol; // The raw prediction (a.k.a. confidence) column name
    public String probabilityCol; //The Column name for predicted class conditional probabilities

    public int maxIter; //Param for maximum number of iterations (>= 0).
    public double regParam; //Param for regularization parameter (>= 0).
    public boolean fitIntercept; //Param for whether to fit an intercept term.
    public boolean standardization; //Param for whether to standardize the training features before fitting the model.

    public double elasticNetParam; //Param for the ElasticNet mixing parameter, in range [0, 1]. For alpha = 0, the penalty is an L2 penalty. For alpha = 1, it is an L1 penalty.
    public double tol; //convergence tolerance for iterative algorithms

    public double threshold; //threshold in binary classification prediction, in range [0, 1].
    public String weightCol; // weight column name. If this is not set or empty, we treat all instance weights as 1.0..

    // grid search parameters
    public String regParamGrid = null;
    public String elasticNetGrid = null;

    public NodeTestLogisticRegression() {}

    public NodeTestLogisticRegression(int i, String nm) {
        super(i, nm);
    }

    //--------------------------------------------------------------------------------------

    public boolean passParamMapToNextNodes(JobContext jobContext, LogisticRegression lr) {

        // grid builder
        ParamGridBuilder gridBuilder = new ParamGridBuilder();

        // reg param grid
        double[] regParamGridDouble = ParseDouble.toDoubleArray(regParamGrid);
        if (regParamGridDouble.length > 0) {
            gridBuilder.addGrid(lr.regParam(), regParamGridDouble);
        }

        // elastic net param grid
        double[] elasticNetGridDouble = ParseDouble.toDoubleArray(elasticNetGrid);
        if (elasticNetGridDouble.length > 0) {
            gridBuilder.addGrid(lr.elasticNetParam(),elasticNetGridDouble);
        }

        // create ParamMap
        ParamMap[] paramGrid = gridBuilder.build();

        // pass param map to the next nodes
        boolean result = passParamMapToNextNodes(jobContext, paramGrid);

        return result;
    }

    //--------------------------------------------------------------------------------------

    @Override
    public void execute(JobContext jobContext) throws Exception {

        LogisticRegression lr = new LogisticRegression();

        lr.setFeaturesCol(featuresCol);
        lr.setLabelCol(labelCol);

        if(maxIter != 100){
            lr.setMaxIter(maxIter);
        }
        if(!fitIntercept){
            lr.setFitIntercept(fitIntercept);
        }
        if(!standardization){
            lr.setStandardization(standardization);
        }
        if(regParam != 0.0){
            lr.setRegParam(regParam);
        }
        if(elasticNetParam != 0.0){
            lr.setElasticNetParam(elasticNetParam);
        }
        if(tol != 1E-6){
            lr.setTol(tol);
        }


        if(predictionCol != null && predictionCol.trim().length() > 0){
            lr.setPredictionCol(predictionCol);
        }
        if(probabilityCol != null && probabilityCol.trim().length() > 0){
            lr.setProbabilityCol(probabilityCol);
        }
        if(rawPredictionCol != null && rawPredictionCol.trim().length() > 0){
            lr.setRawPredictionCol(rawPredictionCol);
        }
        if(weightCol != null && weightCol.trim().length() > 0){
            lr.setWeightCol(weightCol);
        }

        if(threshold != 0.5){
            lr.setThreshold(threshold);
        }

        //--------------------------------------------------------------------------------------------------------------

        // pass pipeline stage
        boolean passedToPipeline = passPipelineStageToNextNodes(jobContext, lr);
        // pass estimator
        boolean passedEstimator = passEstimatorToNextNodes(jobContext, lr);
        // pass grid search parameters
        boolean passedParamMap = passParamMapToNextNodes(jobContext, lr);

        // if there is a pipeline, cross validation or train/validation/split which would handle the processing of this node
        if (passedToPipeline || passedEstimator || passedParamMap) {
            passDataFrameToNextNodesAndExecute(jobContext, dataFrame);

            return;
        }

        //--------------------------------------------------------------------------------------------------------------

        if (dataFrame != null) {

            // fit the model
            LogisticRegressionModel model = lr.fit(dataFrame);

            // output the model
            String[] colNames = SchemaUtil.getColNamesForVectorAssembler(dataFrame, featuresCol);
            jobContext.workflowctx().outLogisticRegressionModel(this, colNames, model);

            // pass model to the next nodes
            passModelToNextNodes(jobContext, model);

            // pass dataframe to the next nodes
            passDataFrameToNextNodes(jobContext, dataFrame);
        }

        // execute the next nodes if they can be executed
        executeNextNodes(jobContext, dataFrame);
    }

    //--------------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------------

}
