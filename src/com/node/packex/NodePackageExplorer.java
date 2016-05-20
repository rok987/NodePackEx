package com.node.packex;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.node.packex.connector.NodeBigQueryConnector;
import com.node.packex.manager.NodeMonthManager;
import com.node.packex.model.Date;
import com.node.packex.model.Month;
import com.node.packex.model.Year;
import com.packex.loader.CompanyLoader;
import com.packex.model.company.CompanyPackages;
import com.packex.model.company.PackageInfo;

public class NodePackageExplorer {
    private static final Logger logger = Logger.getLogger(NodePackageExplorer.class.getName());
    
    public void execute() {
        NodeBigQueryConnector connector = NodeBigQueryConnector.getInstance();
        connector.createDataset(NodeUtil.getNodeDatasetName());
        
        CompanyLoader companyLoader = new CompanyLoader();
        companyLoader.load(NodeConstants.NODE_PACKAGES_FILE_PATH);
        LinkedList<CompanyPackages> companyPackagesList = companyLoader.getCompanyData();
        try {
            connector.createMonthTable(NodeUtil.getNodeDatasetName(), NodeUtil.getNodeTableName(NodeConstants.MONTH));
            connector.begin(NodeUtil.getNodeDatasetName(), NodeUtil.getNodeTableName(NodeConstants.MONTH));
            
            for (CompanyPackages companyPackages : companyPackagesList) {
                for (PackageInfo pkg : companyPackages.getPackages()) {
                    
                    logger.log(Level.INFO, String.format("Saving the package info for %s in the %s language", 
                            pkg.getName().toUpperCase(), pkg.getLanguage().toUpperCase()));
                    
                    // Start in July 2015
                    Month startMonth = NodeUtil.getMonthList().get(7);
                    Year startYear = new Year("2015", false);
                    Date date = new Date(startMonth, startYear);
                    
                    NodeMonthManager monthManager = 
                            new NodeMonthManager(connector, date, pkg.getName(), companyPackages.getCompany(), pkg.getCategory());
                    monthManager.saveData();
                }
            }
            
            connector.commit();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "hit an issue with saving data", ex);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("salut");
    }
}