package org.aksw.fox.nerlearner.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.data.encode.EntityTypesToBILOUEncoding;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class EntitiesToInstances {
  public static Logger LOG = Logger.getLogger(EntitiesToInstances.class);

  private final Map<String, Attribute> attributeNameToAttribute = new HashMap<>();

  public EntitiesToInstances() {}

  // not implemented yet
  protected Map<String, String> mapOracleToBILOU(final Map<String, String> oracle,
      final String splitregText) {

    if (oracle == null) {
      return null;
    } else {
      final Map<String, String> oracleBILOU = new HashMap<>();
      // for all entries
      for (final Entry<String, String> entry : oracle.entrySet()) {
        final String type = entry.getValue();
        // split to tokens
        final String[] split = entry.getKey().split(splitregText);
        if (split.length == 1) {
          // bilou unit
          oracleBILOU.put(split[0], EntityTypesToBILOUEncoding.toUnit(type));
        } else {
          for (int i = 0; i < split.length; i++) {
            String s = split[i];
            s = s.trim();
            if (s.isEmpty()) {
              LOG.warn("Empty token!!");
            }
            if (i == 0) {
              // bilou begin
              oracleBILOU.put(s, EntityTypesToBILOUEncoding.toBegin(type));
            } else if (i + 1 == split.length) {
              // bilou last
              oracleBILOU.put(s, EntityTypesToBILOUEncoding.toLast(type));
            } else {
              // bilou inside
              oracleBILOU.put(s, EntityTypesToBILOUEncoding.toInside(type));
            }
          }
        }
      }

      return oracleBILOU;
    }
  }

  // not implemented yet
  protected Map<String, Map<String, String>> mapResultsToBILOU(//
      final Map<String, Set<Entity>> toolResults, final String splitregText) {

    final Map<String, Map<String, String>> toolResultsBILOU = new HashMap<>();
    if (toolResults == null) {
      return null;
    } else {
      for (final Entry<String, Set<Entity>> entry : toolResults.entrySet()) {
        final String toolname = entry.getKey();
        toolResultsBILOU.put(toolname, new HashMap<>());
        final Set<Entity> entities = entry.getValue();
        for (final Entity entity : entities) {
          final String mention = entity.getText();
          final String type = entity.getType();
          toolResultsBILOU.get(toolname).put(mention, type);
        }
        toolResultsBILOU.put(//
            toolname, //
            mapOracleToBILOU(toolResultsBILOU.get(toolname), splitregText)//
        );
      }
      return toolResultsBILOU;
    }
  }

  public Instances getInstances(//
      final Set<String> tokens, final Map<String, Set<Entity>> toolResults,
      final Map<String, String> oracle) {

    final Map<String, String> oracleBILOU = mapOracleToBILOU(oracle, FoxTextUtil.tokenSpliter);
    final Map<String, Map<String, String>> toolResultsBILOU =
        mapResultsToBILOU(toolResults, FoxTextUtil.tokenSpliter);

    if (oracleBILOU != null) {
      oracleBILOU.entrySet().stream().forEach(LOG::info);
    }
    toolResultsBILOU.entrySet().stream().forEach(LOG::info);

    final Set<String> sortedToolNames = new TreeSet<>(toolResults.keySet());
    final Set<String> sortedTypes = new TreeSet<>(BILOUEncoding.AllTypesSet);

    final List<String> tokensList = new ArrayList<>(tokens);

    final FastVector featureVector;
    featureVector = getFeatureVector(sortedToolNames, sortedTypes);

    final Instances instances;
    {
      final String name = oracleBILOU != null ? "train data" : "test data";
      instances = new Instances(name, featureVector, tokens.size());
      instances.setClassIndex(featureVector.size() - 1);
    }

    int diffNull = 0;
    // sets results of the tools
    for (int i = 0; i < tokensList.size(); i++) {
      final Instance row = new Instance(instances.numAttributes());
      final String token = tokensList.get(i);
      for (final String sortedToolName : sortedToolNames) {

        // set all attributes to 0
        for (final String sortedType : sortedTypes) {
          final String attributeName = sortedToolName + sortedType;
          row.setValue(attributeNameToAttribute.get(attributeName), 0.0);
        }

        // set attributes of the tools
        final String type = toolResultsBILOU.get(sortedToolName).get(token);
        if (type != null && !type.isEmpty()) {
          final String attributeName = sortedToolName + type;
          row.setValue(attributeNameToAttribute.get(attributeName), 1.0);
        }
      }

      // set oracle
      if (oracleBILOU != null) {
        final String value;
        value = oracleBILOU.get(token) == null ? BILOUEncoding.O : oracleBILOU.get(token);
        final Attribute classatt;
        classatt = (Attribute) featureVector.elementAt(instances.numAttributes() - 1);
        row.setValue(classatt, value);

        if (!value.equals(BILOUEncoding.O)) {
          diffNull++;
        }
      }

      instances.add(row);
    }
    LOG.info("# instances: " + instances.numInstances());
    LOG.debug("#instances with a type: " + diffNull);
    if (oracleBILOU != null) {
      LOG.debug("found all (should be true): " + (diffNull == oracleBILOU.size()));
    }

    return instances;
  }

  protected FastVector getFeatureVector(//
      final Set<String> sortedToolNames, final Set<String> sortedTypes) {

    // declare the feature vector
    final FastVector fv = new FastVector();

    // declare numeric attribute along with its values
    for (final String sortedToolName : sortedToolNames) {
      for (final String cl : sortedTypes) {
        final String attributeName = sortedToolName + cl;
        attributeNameToAttribute.put(attributeName, new Attribute(attributeName));
        fv.addElement(attributeNameToAttribute.get(attributeName));
      }
    }

    // declare the class attribute along with its values
    final FastVector attVals = new FastVector();
    sortedTypes.stream().forEach(attVals::addElement);

    // class att. at last position!
    fv.addElement(new Attribute("class", attVals));
    return fv;
  }
}
