/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.visualization.bridge;

import java.util.Iterator;
import org.gephi.data.network.api.EdgeWrap;
import org.gephi.data.network.api.NodeWrap;
import org.gephi.data.network.reader.MainReader;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Object3d;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.Object3dInitializer;
import org.gephi.visualization.opengl.AbstractEngine;




/**
 *
 * @author Mathieu
 */
public class DHNSDataBridge implements DataBridge, VizArchitecture {

    //Architecture
    protected AbstractEngine engine;
    protected MainReader reader;
    private VizConfig vizConfig;

    //Attributes
    private int cacheMarker = 0;

    @Override
    public void initArchitecture() {
       this.engine = VizController.getInstance().getEngine();
       this.reader = new MainReader();
       this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void updateWorld() {
        cacheMarker++;
        if(reader.requireNodeUpdate())
            updateNodes();

        if(reader.requireEdgeUpdate())
            updateEdges();
        
        engine.worldUpdated(cacheMarker);
    }


    private void updateNodes()
    {
        Object3dInitializer nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentObject3dInitializer();

        Iterator<? extends NodeWrap> itr = reader.getNodes();
        for(;itr.hasNext();itr.next())
        {
            NodeWrap preNode = itr.next();
            Node node=preNode.getNode();

            Object3d obj = node.getObject3d();
            if(obj==null)
            {
                //Object3d is null, ADD
                obj = nodeInit.initObject(node);
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl)obj);
            }
            obj.setCacheMarker(cacheMarker);

            node.setSize(10f);
        }
    }

    private void updateEdges()
    {
        Object3dInitializer edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentObject3dInitializer();
        Object3dInitializer arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentObject3dInitializer();

        Iterator<? extends EdgeWrap> itr = reader.getEdges();
        for(;itr.hasNext();itr.next())
        {
            EdgeWrap virtualEdge = itr.next();
            Edge edge = virtualEdge.getEdge();

            Object3d obj = edge.getObject3d();
            if(obj==null)
            {
                //Object3d is null, ADD
                obj = edgeInit.initObject(edge);
                engine.addObject(AbstractEngine.CLASS_EDGE, (Object3dImpl)obj);
                if(vizConfig.isDirectedEdges())
                {
                    Object3d arrowObj = arrowInit.initObject(edge);
                    engine.addObject(AbstractEngine.CLASS_ARROW, (Object3dImpl)arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }
    }

    public boolean requireUpdate() {
        return reader.requireUpdate();
    }
    
}
