# Meast-Toolkit is a toolkit for multi-entity-relation finding.
/MEAST contains the whole project of the toolkit.</br>
/example contains an example entity-relation graph.</br>
/example/ExampleGraph.pdf is how the exapmle entity-relation graph look like.</br>
/example/ExampleTriples is the source data of the example.</br>
/meast.jar the runnable jar file. To run the example correctly, you have to put the example folder in the current working directory. 
</br></br>
To running a completed progress, you may wish to run </br>'java -cp meast.jar name.dxliu.exapmle.Step1_ExampleTriplePreprocessor'</br> for
the preprocessing and run </br>'java -cp meast.jar name.dxliu.exapmle.Step2_ExampleOracleUsage'</br> to construct distance oracle.Then </br>'java -cp meast.jar name.dxliu.exapmle.Step1_ExampleTriplePreprocessor'</br>
is how to query the relation between Alice,Bob and Chris in the example graph.
