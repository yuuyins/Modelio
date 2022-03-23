/* 
 * Copyright 2013-2020 Modeliosoft
 * 
 * This file is part of Modelio.
 * 
 * Modelio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Modelio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Modelio.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.modelio.diagram.elements.core.link.anchors.fixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.modeliosoft.modelio.javadesigner.annotations.objid;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.AccessibleAnchorProvider;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.modelio.diagram.elements.core.figures.anchors.DelegateAnchor;
import org.modelio.diagram.elements.core.figures.anchors.FixedAnchor;
import org.modelio.diagram.elements.core.figures.anchors.IFixedAnchorLocator;
import org.modelio.diagram.elements.core.figures.anchors.SatelliteAnchor;
import org.modelio.diagram.elements.core.figures.geometry.Direction;
import org.modelio.diagram.elements.core.link.CreateBendedConnectionRequest;
import org.modelio.diagram.elements.core.link.GmAbstractLinkAnchor;
import org.modelio.diagram.elements.core.link.RoutingModeGetter;
import org.modelio.diagram.elements.core.link.anchors.AbstractNodeAnchorProvider;
import org.modelio.diagram.elements.core.link.anchors.AnchorRefHelper;
import org.modelio.diagram.elements.core.link.anchors.GmBorderAnchor;
import org.modelio.diagram.elements.core.link.anchors.GmFixedAnchor;
import org.modelio.diagram.elements.core.link.anchors.GmNodeAnchor;
import org.modelio.diagram.elements.core.link.anchors.GmRaySlidableAnchor;
import org.modelio.diagram.elements.core.link.anchors.GmSourceSatelliteAnchor;
import org.modelio.diagram.elements.core.link.anchors.GmXYAnchor;
import org.modelio.diagram.elements.core.link.anchors.RectangleNodeAnchorProvider;
import org.modelio.diagram.elements.core.model.IGmLinkObject;
import org.modelio.diagram.elements.core.model.IGmPath;
import org.modelio.diagram.elements.core.requests.CreateLinkConstants;
import org.modelio.diagram.elements.plugin.DiagramElements;
import org.modelio.diagram.styles.core.StyleKey.ConnectionRouterId;

/**
 * Provides connection anchors for BPMN nodes.
 * <p>
 * <b>Usage:</b><br>
 * <code>
 * srcAnchor = BpmnNodeAnchorProvider.get(connEditPart).getSourceConnectionAnchor(srcNodeEditPart);
 * targetAnchor = BpmnNodeAnchorProvider.get(connEditPart).getSourceConnectionAnchor(targetNodeEditPart);
 * </code>
 * <p>
 * The returned provider should be hold by the node edit part to avoid constant computations.
 * <p>
 * Sub classes must implement
 * <ul>
 * <li>The {@link IFixedConnectionAnchorFactory} interface methods.
 * </ul>
 * 
 * @author cmarin
 * @since 5.0.2
 */
@objid ("b70ed3a6-0ed3-4c72-b0eb-4ecfb11cc6bd")
public abstract class AbstractFixedNodeAnchorProvider extends AbstractNodeAnchorProvider implements IFixedConnectionAnchorFactory, IFixedAnchorLocator {
    /**
     * Create a serializable anchor model from the given anchor.
     * @param anchor a figure anchor
     * @return an anchor model.
     */
    @objid ("a6b1c162-f340-4e81-a549-59de048215aa")
    @Override
    public Object createAnchorModel(final ConnectionAnchor anchor) {
        if (anchor instanceof ChopboxAnchor) {
            return null;
        } else if (anchor instanceof EllipseAnchor) {
            return null;
        } else if (anchor instanceof FixedAnchor) {
            FixedAnchor fa = (FixedAnchor) anchor;
            return new GmFixedAnchor(fa.getFace(), fa.getRank(), fa.getTotalOnFace());
        } else if (anchor instanceof XYAnchor) {
            XYAnchor xy = (XYAnchor) anchor;
            return new GmXYAnchor(xy.getReferencePoint());
        } else if (anchor instanceof DelegateAnchor) {
            DelegateAnchor a = (DelegateAnchor) anchor;
            return createAnchorModel(a.getDelegate());
        } else if (anchor instanceof SatelliteAnchor) {
            SatelliteAnchor sa = (SatelliteAnchor) anchor;
            GmSourceSatelliteAnchor ret = new GmSourceSatelliteAnchor();
            ret.setLocation(sa.getDistance());
            return ret;
        } else {
            // Compatibility with <= 5.0.0 diagrams
            return RectangleNodeAnchorProvider.get().createAnchorModel(anchor);
        }
        
    }

    /**
     * <p>
     * Get an implementation of {@link AccessibleAnchorProvider} for the given node figure.
     * </p>
     * @param nodeFig a node figure
     * @return an implementation of {@link AccessibleAnchorProvider}
     */
    @objid ("1dcc11db-e367-4e01-bd14-b53d147abf5e")
    @Override
    public AccessibleAnchorProvider getAccessibleAnchorProvider(IFigure nodeFig) {
        return new AccessibleAnchorProvider() {
            @objid ("6ec0672d-aeaf-4655-987d-4150507d600b")
            @Override
            public List getSourceAnchorLocations() {
                return getTargetAnchorLocations();
            }
        
            @objid ("1bd01c33-3d6d-4108-ae55-84b7242c9220")
            @Override
            public List getTargetAnchorLocations() {
                Collection<ConnectionAnchor> allAnchors = getAllAnchors(nodeFig, ConnectionRouterId.ORTHOGONAL, null);
                List<Point> ret = new ArrayList<>(allAnchors.size());
        
                for (ConnectionAnchor a : allAnchors) {
                    ret.add(a.getReferencePoint());
                }
                return ret;
            }
        };
        
    }

    /**
     * Returns the <code>ConnectionAnchor</code> for the specified <i>source</i> rectangular node. The NodeEditPart is the {@link ConnectionEditPart#getSource() source} EditPart for the provider connection.
     * <P>
     * The anchor may be a function of the connection's model, the node's model, a combination of both, or it may not depend on anything all.
     * @param nodeEditPart The rectangular node to anchor from
     * @param connEditpart The connection to anchor from.
     * @return the ConnectionAnchor for the given rectangular EditPart
     */
    @objid ("cfb42fde-574d-403b-8ab8-b4039f0f4ad4")
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(final GraphicalEditPart nodeEditPart, final ConnectionEditPart connEditpart) {
        State state = new State(connEditpart);
        if (state.gmSourceAnchor == null) {
            return new ChopboxAnchor(nodeEditPart.getFigure());
        } else {
            ConnectionAnchor ret = updateConnectionAnchor(state, nodeEditPart, true);
            return ret;
        }
        
    }

    /**
     * Returns the <code>ConnectionAnchor</code> for the specified <i>target</i> rectangular node. The NodeEditPart is the {@link ConnectionEditPart#getTarget() source} EditPart for the provider connection.
     * <P>
     * The anchor may be a function of the connection's model, the node's model, a combination of both, or it may not depend on anything all.
     * @param nodeEditPart The rectangular node to anchor to
     * @param connEditpart The connection to anchor to.
     * @return the ConnectionAnchor for the given rectangular EditPart
     */
    @objid ("88881137-1bfa-4e27-8566-f7e354129e47")
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(final GraphicalEditPart nodeEditPart, final ConnectionEditPart connEditpart) {
        State state = new State(connEditpart);
        
        if (state.gmTargetAnchor == null) {
            return new ChopboxAnchor(nodeEditPart.getFigure());
        } else {
            ConnectionAnchor ret = updateConnectionAnchor(state, nodeEditPart, false);
            return ret;
        }
        
    }

    /**
     * protected only constructor.
     */
    @objid ("7e60f205-a4bc-46d5-b195-e6fcdff1d9b1")
    protected  AbstractFixedNodeAnchorProvider() {
        
    }

    @objid ("d46b26d2-f8c7-4a37-867f-3f18962667b9")
    @Override
    protected ConnectionAnchor getAnchorForCreateBendedConnectionRequest(GraphicalEditPart nodeEditPart, CreateBendedConnectionRequest req, boolean isSourceAnchor) {
        IFigure figure = nodeEditPart.getFigure();
        Rectangle figureBounds = figure.getBounds().getCopy();
        figure.translateToAbsolute(figureBounds);
        Point figCenter = figureBounds.getCenter();
        
        if (isSourceAnchor) {
            Point srcPoint = findGoodAnchorRef(
                    req.getData().getSrcPoint(),
                    req.getData().getPath(),
                    true,
                    req.getData().getLastPoint(),
                    req.getLocation(),
                    figCenter);
        
            if (srcPoint == null) {
                srcPoint = figureBounds.getRight();
            }
            return getNearest(figure, srcPoint, req.getData().getRoutingMode(), null, isSourceAnchor);
        } else {
            // Flag put by CreateRakeLinkEditPolicy.createPathModel(...)
            // this.raked = request.getExtendedData().get("rake") == Boolean.TRUE;
            Point targetPoint = findGoodAnchorRef(
                    req.getLocation(),
                    req.getData().getPath(),
                    false,
                    req.getData().getSrcPoint(),
                    null,
                    figCenter);
            if (targetPoint == null) {
                targetPoint = figureBounds.getLeft();
            }
            return getNearest(figure, targetPoint, req.getData().getRoutingMode(), null, isSourceAnchor);
        }
        
    }

    @objid ("3e53fa20-f4f1-4c7c-ac97-02937531a5f0")
    private static Point findGoodAnchorRef(Point candidate1, List<Point> candidates2, boolean first, Point candidate3, Point candidate4, Point forbidden) {
        if (AnchorRefHelper.isGoodAnchorRef(candidate1, forbidden))
            return candidate1;
        
        Point found = AnchorRefHelper.findGoodAnchorRef(candidates2, first, 1, forbidden);
        if (found != null)
            return found;
        
        if (AnchorRefHelper.isGoodAnchorRef(candidate3, forbidden))
            return candidate3;
        
        if (AnchorRefHelper.isGoodAnchorRef(candidate4, forbidden))
            return candidate4;
        return null;
    }

    /**
     * Returns the <code>ConnectionAnchor</code> for the specified Request. The returned ConnectionAnchor is used only when displaying <i>feedback</i>.
     * @param request a Request describing the current interaction
     * @param source Whether an anchor is needed for a source or a target side.
     * @return the ConnectionAnchor to use during feedback
     * @throws IllegalArgumentException if the request has no location.
     */
    @objid ("690b86b0-c754-4506-b0ee-eb5fcc4a84c4")
    @Override
    protected ConnectionAnchor getAnchorForCreateRequest(final GraphicalEditPart nodeEditPart, final CreateRequest request, final boolean source) throws IllegalArgumentException {
        final Point p = request.getLocation();
        return getNearest(nodeEditPart.getFigure(), p, ConnectionRouterId.ORTHOGONAL, null, source);
    }

    @objid ("e4a9950b-deb6-496b-b57b-99b9b90dcb01")
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(GraphicalEditPart nodeEditPart, Request request) {
        Boolean needSlidableAnchor = (Boolean) request.getExtendedData().get(CreateLinkConstants.PROP_NEED_SLIDABLE_ANCHOR);
        
        if (Boolean.TRUE.equals(needSlidableAnchor)) {
            return RectangleNodeAnchorProvider.getSlidable().getSourceConnectionAnchor(nodeEditPart, request);
        } else {
            return super.getSourceConnectionAnchor(nodeEditPart, request);
        }
        
    }

    @objid ("1996f25e-f5af-480d-9789-84bfeb9cc5e2")
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(GraphicalEditPart nodeEditPart, Request request) {
        Boolean needSlidableAnchor = (Boolean) request.getExtendedData().get(CreateLinkConstants.PROP_NEED_SLIDABLE_ANCHOR);
        
        if (Boolean.TRUE.equals(needSlidableAnchor)) {
            return RectangleNodeAnchorProvider.getSlidable().getTargetConnectionAnchor(nodeEditPart, request);
        } else {
            return super.getTargetConnectionAnchor(nodeEditPart, request);
        }
        
    }

    /**
     * Returns the <code>ConnectionAnchor</code> for the specified Request. The returned ConnectionAnchor is used only when displaying <i>feedback</i>. The Request is usually a {@link org.eclipse.gef.requests.LocationRequest} , which provides the current
     * mouse location.
     * @param request a Request describing the current interaction
     * @param source Whether an anchor is needed for a source or a target side.
     * @return the ConnectionAnchor to use during feedback
     * @throws IllegalArgumentException if the request has no location.
     */
    @objid ("f57ce3d3-89db-4af1-b37a-b06e9d765364")
    @Override
    protected ConnectionAnchor getAnchorForReconnectRequest(final GraphicalEditPart nodeEditPart, final ReconnectRequest request, final boolean source) throws IllegalArgumentException {
        final Point requestLocation = request.getLocation();
        if (requestLocation == null) {
            throw new IllegalArgumentException(request + " has no location.");
        }
        
        State state = new State(request.getConnectionEditPart());
        
        final EditPart oldNodeEditPart = source ? state.connectionEditPart.getSource()
                : state.connectionEditPart.getTarget();
        
        IFigure nodeFigure = (IFigure) request.getExtendedData().get(CreateLinkConstants.PROP_RECONNECT_ON_FIGURE);
        if (nodeFigure == null) {
            nodeFigure = nodeEditPart.getFigure();
        }
        
        if (oldNodeEditPart == nodeEditPart) {
            // The connection stayed on the same source/target.
            // The request may tell to stay on same face.
            // Try to return the same previous instance to spare listeners firing & changes
        
            Connection connFig = state.getConnection();
            ConnectionAnchor refAnchor = (ConnectionAnchor) request.getExtendedData().get(CreateLinkConstants.PROP_RECONNECT_ON_SAME_FACE);
            ConnectionAnchor previous;
        
            if (refAnchor != null) {
                previous = refAnchor;
            } else if (source) {
                previous = connFig.getSourceAnchor();
            } else {
                previous = connFig.getTargetAnchor();
            }
        
            Integer face = refAnchor instanceof FixedAnchor ? ((FixedAnchor) refAnchor).getFace() : null;
        
            ConnectionAnchor nearest = getNearest(nodeFigure, requestLocation, state.getFigureRoutingMode(), face, source);
            if (nearest == null) {
                nearest = getNearest(nodeFigure, requestLocation, state.getFigureRoutingMode(), null, source);
            }
        
            if (nearest == null) {
                return previous;
            } else if (previous != null && previous.equals(nearest)) {
                // return same previous instance to spare listeners firing & changes
                return previous;
            } else {
                return nearest;
            }
        } else {
            ConnectionRouterId routerId = state.getFigureRoutingMode();
            return getNearest(nodeFigure, requestLocation, routerId, null, source);
        }
        
    }

    @objid ("1ccc8d53-bbf5-4510-b634-6dee50fed37c")
    private boolean isRaked(final IGmPath p) {
        return p.getSourceRake() != null || p.getTargetRake() != null;
    }

    /**
     * Update an existing connection anchor from a given anchor model and a connection routing mode.
     * <p>
     * May return the same connection anchor or another one. In the last case the returned anchor must be used and the other should be discarded.
     * @param nodeEditPart The rectangular node to anchor to
     * @param isSourceAnchor <code>true</code> if the anchor is a source anchor, <code>false</code> if it is a target anchor
     * @return the updated draw2d anchor, or a new one.
     */
    @objid ("36a35838-5bfb-4353-af1a-40c91ca09fbb")
    private ConnectionAnchor updateConnectionAnchor(final State state, final GraphicalEditPart nodeEditPart, final boolean isSourceAnchor) {
        final Object gmLinkAnchor = !isSourceAnchor ? state.gmTargetAnchor : state.gmSourceAnchor;
        
        final IFigure nodeFigure = nodeEditPart.getFigure();
        if (gmLinkAnchor instanceof GmRaySlidableAnchor || gmLinkAnchor instanceof GmNodeAnchor  || gmLinkAnchor instanceof GmBorderAnchor) {
            // Compatibility with <= 5.0.0 diagrams
            if (isSourceAnchor) {
                return RectangleNodeAnchorProvider.getSlidable().getSourceConnectionAnchor(nodeEditPart, state.connectionEditPart);
            } else {
                return RectangleNodeAnchorProvider.getSlidable().getTargetConnectionAnchor(nodeEditPart, state.connectionEditPart);
            }
        } else if (gmLinkAnchor instanceof GmFixedAnchor) {
            GmFixedAnchor a = (GmFixedAnchor) gmLinkAnchor;
            return createFromModel(nodeFigure, a);
        } else if (gmLinkAnchor instanceof GmXYAnchor) {
            final GmXYAnchor pa = (GmXYAnchor) gmLinkAnchor;
            final Point p = pa.getReferencePoint();
                return new XYAnchor(p);
        } else if (gmLinkAnchor instanceof GmSourceSatelliteAnchor) {
            GmSourceSatelliteAnchor sa = (GmSourceSatelliteAnchor) gmLinkAnchor;
            IFigure srcFig;
            if (isSourceAnchor) {
                srcFig = ((GraphicalEditPart) state.connectionEditPart.getTarget()).getFigure();
            } else {
                srcFig = ((GraphicalEditPart) state.connectionEditPart.getSource()).getFigure();
            }
        
            return new SatelliteAnchor(srcFig, sa.getLocation());
        } else if (gmLinkAnchor instanceof GmAbstractLinkAnchor) {
            // should not happen, convert unsupported GM to supported anchor
            DiagramElements.LOG.debug(new IllegalArgumentException(String.format("Unsupported anchor <%s> - on %s - for %s connection", gmLinkAnchor, nodeEditPart, state.connectionEditPart)));
            GmAbstractLinkAnchor a = (GmAbstractLinkAnchor) gmLinkAnchor;
            Point pos = nodeFigure.getBounds().getTopLeft().translate(a.getLocation());
            nodeFigure.translateToAbsolute(pos);
            return getNearest(nodeEditPart.getFigure(), pos, state.getFigureRoutingMode(), null, isSourceAnchor);
        } else {
            // should not happen, ask for anchor to node center
            DiagramElements.LOG.debug(new IllegalArgumentException(String.format("Unknown anchor <%s> - on %s - for %s connection", gmLinkAnchor, nodeEditPart, state.connectionEditPart)));
            Point pos = nodeFigure.getBounds().getCenter();
            nodeFigure.translateToAbsolute(pos);
            return getNearest(nodeEditPart.getFigure(), pos, state.getFigureRoutingMode(), null, isSourceAnchor);
        
        }
        
    }

    /**
     * Some computations and quick references about the current ConnectionEditPart.
     */
    @objid ("76bb1cd5-3c94-4714-826d-3ec3a72cfac7")
    private static class State {
        @objid ("4016447b-8831-48e2-8b38-59b648aed418")
        final ConnectionEditPart connectionEditPart;

        @objid ("947aec4e-99f0-4658-8c62-9df8035a1006")
        final IGmLinkObject gmLink;

        @objid ("407e77a6-7718-4a1a-8bed-966017aaeb72")
        final Object gmSourceAnchor;

        @objid ("204bc5e2-4553-4a96-a56e-9d9dc4ce5155")
        final Object gmTargetAnchor;

        @objid ("a7c7f730-074d-4a1b-b146-d316eaf4b80e")
        public  State(final ConnectionEditPart aConnectionEditPart) {
            this.connectionEditPart = aConnectionEditPart;
            this.gmLink = (IGmLinkObject) aConnectionEditPart.getModel();
            
            final IGmPath path = this.gmLink.getPath();
            this.gmSourceAnchor = path.getSourceAnchor();
            this.gmTargetAnchor = path.getTargetAnchor();
            // this.raked = isRaked(path);
            
        }

        @objid ("7849736f-121d-46f5-b95a-47bfc241e611")
        public Connection getConnection() {
            return (Connection) this.connectionEditPart.getFigure();
        }

        /**
         * Get the routing mode from the connection figure.
         * @return the connection routing mode
         */
        @objid ("b027ee31-ca5a-4291-ad6d-692c897bd6c5")
        public ConnectionRouterId getFigureRoutingMode() {
            return RoutingModeGetter.fromEditPart(this.connectionEditPart);
        }

    }

    /**
     * {@link #getLocation(FixedAnchor, Point)} snaps anchor location to the given reference point if within tolerance.
     */
    @objid ("4a1bc576-4359-4a02-b9a4-2c3174d3a55c")
    protected static class TolerantLocator implements IFixedAnchorLocator {
        @objid ("2cc7555f-bfb1-4d4f-86a2-f3609c36a774")
        private final int tolerance;

        @objid ("19e9897f-83f7-48cc-8ebe-5c58062eb307")
        private IFixedAnchorLocator delegate;

        @objid ("d1870459-15d9-46f7-b2d4-e73240404a06")
        public  TolerantLocator(IFixedAnchorLocator delegate, int tolerance) {
            super();
            this.delegate = delegate;
            this.tolerance = tolerance;
            
        }

        @objid ("5aefc791-ea8b-447e-ae6a-29ed26472dc9")
        @Override
        public Point getReferencePoint(FixedAnchor anchor) {
            return this.delegate.getReferencePoint(anchor);
        }

        @objid ("149f8c34-c98d-49c0-a2d1-1f7e46b90fcb")
        @Override
        public String getFaceName(FixedAnchor fixedAnchor) {
            return this.delegate.getFaceName(fixedAnchor);
        }

        @objid ("37ea2b54-3ca3-4749-a3d2-a6ffe2e8d56c")
        @Override
        public void onFigureMoved(IFigure figure) {
            this.delegate.onFigureMoved(figure);
        }

        @objid ("e30b8b52-1991-4d3c-bf4e-fbaa4be01c08")
        @Override
        public Direction getDirection(FixedAnchor anchor) {
            return this.delegate.getDirection(anchor);
        }

        @objid ("2352c14a-b002-458c-b5c2-8af4668ca05b")
        @Override
        public Point getLocation(FixedAnchor anchor, Point reference) {
            Point location = this.delegate.getLocation(anchor, reference);
            
            // snap anchor location to the given reference point if within tolerance.
            Dimension tolerance2d = new PrecisionDimension(this.tolerance, this.tolerance);
            anchor.getOwner().translateToAbsolute(tolerance2d);
            
            double d;
            switch (anchor.getFace()) {
            case FixedNodeAnchorProvider.FACE_EAST:
            case FixedNodeAnchorProvider.FACE_WEST:
                d = reference.preciseY() - location.preciseY();
                if (d > 0 && d > -tolerance2d.preciseHeight() && d <= tolerance2d.preciseHeight()) {
                    return new PrecisionPoint(location.preciseX(), reference.preciseY());
                }
                break;
            case FixedNodeAnchorProvider.FACE_NORTH:
            case FixedNodeAnchorProvider.FACE_SOUTH:
                d = reference.preciseX() - location.preciseX();
                if (d > 0 && d > -tolerance2d.preciseWidth() && d <= tolerance2d.preciseWidth()) {
                    return new PrecisionPoint(reference.preciseX(), location.preciseY());
                }
                break;
            default:
            }
            
            // Return anchor location by default
            return location;
        }

    }

}
