package com.mycompany.cobol.sample.checks;

import com.sonar.sslr.api.AstNode;
import com.sonarsource.cobol.api.ast.CobolCheck;

import java.util.HashMap;
import java.util.Map;

import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * Custom rule to check the column positions of TO statements within a paragraph.
 */
@Rule(
    key = "ToKeywordPositionCheck",
    name = "TO Statements Column Check",
    priority = Priority.CRITICAL,
    tags = {"convention"}
)
public class ToKeywordPositionCheck extends CobolCheck {

    @Override
    public void init() {
        subscribeTo(getCobolGrammar().paragraph);
    }

    @Override
    public void visitNode(AstNode paragraphNode) {
        Map<Integer, Integer> columnCountMap = new HashMap<>();

        for (AstNode moveStatementNode : paragraphNode.getChildren(getCobolGrammar().moveStatement)) {
            AstNode toKeywordAstNode = moveStatementNode.getFirstDescendant(getCobolGrammar().dataValueTo);

            if (toKeywordAstNode != null) {
                int toColumn = toKeywordAstNode.getToken().getColumn();

                // Update the column count map
                columnCountMap.put(toColumn, columnCountMap.getOrDefault(toColumn, 0) + 1);
            }
        }

        // Find the column with the maximum count
        int maxColumn = -1;
        int maxCount = 0;
        /* Iteration over the entries of 'colunmCountMap 
          
         * to find the column (maxColumn) with the maximum count of TO statements (maxCount). 
         
         * columnCountMap.entrySet(): This returns a set view of the entries (key-value pairs) contained in the columnCountMap.
         
         *for (Map.Entry<Integer, Integer> entry : columnCountMap.entrySet()): This iterates over each entry in the map.
        
         * if (entry.getValue() > maxCount): This condition checks if the count of TO statements for the current column (entry.getValue()) is greater than the current maximum count (maxCount).
         
         *Inside the if block:
         
         *maxColumn = entry.getKey();: If the count for the current column is greater than the current maximum count, it updates maxColumn with the column of the current entry.
         
         *maxCount = entry.getValue();: It updates maxCount with the count of TO statements for the current column, making it the new maximum count.       
         */
        for (Map.Entry<Integer, Integer> entry : columnCountMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxColumn = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        // Check if the majority of TO statements are in the same column
        for (Map.Entry<Integer, Integer> entry : columnCountMap.entrySet()) {
            if (entry.getKey() != maxColumn && entry.getValue() > maxCount / 2) {
                reportIssue("TO statements should be present in the column "+maxColumn)
                        .on(paragraphNode);
                return;
            }
        }
    }
}
