package com.curba.zonedesigner.client;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Image;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

public class GardenGraphic extends DrawingArea {
	private RegionEntity m_region;
	private GardenTypeEntity m_gardenType;
	private GardenEntity m_entity;
	public GardenEntity getEntity()
	{
		return m_entity;
	}
	
	private ArrayList<ActionEntity> m_actions = new ArrayList<ActionEntity>();
	public ArrayList<ActionEntity> getActions()
	{
		return m_actions;
	}
	
	private ArrayList<ZoneGraphic> m_zones = new ArrayList<ZoneGraphic>();
	public List<ZoneGraphic> getZones()
	{
		return m_zones;
	} 

	private int m_zoom = 0;
	
	private ArrayList<CropGraphic> m_CropsDeleted = new ArrayList<CropGraphic>();
	private ArrayList<ZoneGraphic> m_ZonesDeleted = new ArrayList<ZoneGraphic>();
	
	private JsArray<CropEntity> m_EntityCrops = null;
	public JsArray<CropEntity> getEntityCrops()
	{
		return m_EntityCrops;
	}
	public void setEntityCrops(JsArray<CropEntity> entities)
	{
		m_EntityCrops = entities;
	}
	
	private JsArray<ZoneEntity> m_EntityZones = null;
	public JsArray<ZoneEntity> getEntityZones()
	{
		return m_EntityZones;
	}
	public void setEntityZones(JsArray<ZoneEntity> entities)
	{
		m_EntityZones = entities;
	}
	
	private Image m_backGround;
	
	private Object m_SelectedGraphicObject = null;
	public Object getSelectedGraphicObject()
	{
		return m_SelectedGraphicObject;
	}
	public void setSelectedGraphicObject(Object value)
	{
		m_SelectedGraphicObject = value;
	}
	
	public GardenGraphic(GardenEntity entity) {
		super(entity.getWidth(), entity.getHeight());
		// TODO Auto-generated constructor stub
		
		m_entity = entity;
		
		m_backGround = new Image(0, 0, entity.getWidth(), entity.getHeight(), "http://www.urvangreen.com/images/ground02.jpg");
		this.add(m_backGround);
	}
	
	public void ZoomToOriginal()
	{
		super.setWidth(getGardenEntity().getWidth());
		super.setHeight(getGardenEntity().getHeight());
		
		m_backGround.setWidth(getGardenEntity().getWidth());
		m_backGround.setHeight(getGardenEntity().getHeight());
	}

	public void setGardenEntity(GardenEntity entity) {
		this.m_entity = entity;
	}

	public GardenEntity getGardenEntity() {
		return m_entity;
	}
	
	public void setRegion(JsArray<RegionEntity> regions, int regionId)
	{
		for(int i=0; i<regions.length(); i++)
		{
			RegionEntity r = regions.get(i);
			  
			if (r.getId() == regionId)
			{
				this.m_region = r;
				break;
			}
		}
	}
	public void setGardenType(JsArray<GardenTypeEntity> gardenTypes, int gardenTypeId)
	{
		for(int i=0; i<gardenTypes.length(); i++)
		{
			GardenTypeEntity gt = gardenTypes.get(i);
			  
			if (gt.getId() == gardenTypeId)
			{
				this.m_gardenType = gt;
				
				String imageToShow = "";
				switch(gardenTypeId){
				case 1:
					imageToShow = "http://www.urvangreen.com/images/ground02.jpg";	//Ground
					break;
				case 2:
					imageToShow = "http://www.urvangreen.com/images/balcony01.jpg";	//Balcony
					break;
				case 3:
					imageToShow = "http://www.urvangreen.com/images/roof01.jpg";	//Roof
					break;
				default :
					imageToShow = "http://www.urvangreen.com/images/ground01.jpg";	//Unknown
					break;
				}
				m_backGround.setHref(imageToShow);
				
				break;
			}
		}
	}
	
	public void ReZoom(int zoomModifier)
	{
		if (m_zoom <= 1 && m_zoom >= -1)
		{
			if (zoomModifier < 0) m_zoom = -1 + zoomModifier;
			else m_zoom = 1 + zoomModifier;
		}
		else
		{
			m_zoom = m_zoom + zoomModifier;
		}
		
		//String msg = this.toString() + " Zoom=" + Integer.toString(m_zoom);
		for(int i=0; i<m_zones.size(); i++)
		{
			//msg += " Crops=";
			for(int j=0; j<m_zones.get(i).getCrops().size(); j++)
			{
				m_zones.get(i).getCrops().get(j).Zoom(m_zoom);
				
				//msg += m_zones.get(i).getCrops().get(j).toString() + " ";
			}
			m_zones.get(i).Zoom(m_zoom);
			
			//msg += m_zones.get(i).toString() + " ";
		}
		Zoom(m_zoom);
		
		//Window.alert(msg);
	}
	
	public void Zoom(int zoom)
	{
		//Window.alert(Integer.toString(zoom));
		
		if (zoom == 0) zoom = 1;
		
		if (zoom >= 0)
		{
			super.setWidth(getGardenEntity().getWidth() * zoom);
			super.setHeight(getGardenEntity().getHeight() * zoom);
		}
		else
		{
			super.setWidth(getGardenEntity().getWidth() / Math.abs(zoom));
			super.setHeight(getGardenEntity().getHeight() / Math.abs(zoom));
		}
	}
	
	public int Unzoom(int value)
	{
		if (m_zoom == 0) m_zoom = 1;
		
		if (m_zoom <= 0)
		{
			return value * Math.abs(m_zoom);
		}
		else
		{
			return value / Math.abs(m_zoom);
		}
	}
	
	public void MoveCrop(CropGraphic crop, int zoomedX, int zoomedY)
	{
		ZoneGraphic zone = crop.getZone();
		
		int newX = zoomedX - (crop.getWidth() / 2);
		int newY = zoomedY - (crop.getHeight() / 2);
		
		if (newX < zone.getX()) newX = zone.getX();
		if (newX + crop.getWidth() > zone.getX() + zone.getWidth()) newX = zone.getX() + zone.getWidth() - crop.getWidth();
		if (newX >= zone.getX() && (newX + crop.getWidth() <= zone.getX() + zone.getWidth()))
		{
			crop.setX(newX);
		}
		
		if (newY < zone.getY()) newY = zone.getY();
		if (newY + crop.getHeight() > zone.getY() + zone.getHeight()) newY = zone.getY() + zone.getHeight() - crop.getHeight();				
		if (newY >= zone.getY() && (newY + crop.getHeight() <= zone.getY() + zone.getHeight()))
		{
		    crop.setY(newY);
		}
		
		crop.setPointX(Unzoom(newX));
		crop.setPointY(Unzoom(newY));
		
		//Unzoom x and y
		//int x = Unzoom(zoomedX);
		//int y = Unzoom(zoomedY);
		//		
		//crop.setPointX(x - (Unzoom(crop.getWidth()) / 2));
		//crop.setPointY(y - (Unzoom(crop.getHeight()) / 2));
	}
	
	public void MoveZone(ZoneGraphic zone, int zoomedX, int zoomedY)
	{
		GardenGraphic garden = zone.getGarden();
		
		//Move the zone and his crops
		int oldX = zone.getX();
		int oldY = zone.getY();
		int newX = zoomedX - (zone.getWidth() / 2);
		int newY = zoomedY - (zone.getHeight() / 2);
		
		if (newX < 0) newX = 0;
		if (newX + zone.getWidth() > garden.getWidth()) newX = garden.getWidth() - zone.getWidth();  
		if (newX >= 0 && (newX + zone.getWidth() <= garden.getWidth()))
		{
			zone.setX(newX);
		    //Move each zone crop
		    for (int i=0; i<zone.getCrops().size(); i++)
		    {
		    	CropGraphic c = zone.getCrops().get(i);
		    	c.setX(c.getX() + newX - oldX);
		    	
		    	c.setPointX(Unzoom(c.getX() + newX - oldX));
		    }
		}
		
		if (newY < 0) newY = 0;
		if (newY + zone.getHeight() > garden.getHeight()) newY = garden.getHeight() - zone.getHeight();  
		if (newY >= 0 && (newY + zone.getHeight() <= garden.getHeight()))
		{
		    zone.setY(newY);
		    //Move each zone crop
		    for (int i=0; i<zone.getCrops().size(); i++)
		    {
		    	CropGraphic c = zone.getCrops().get(i);
		    	c.setY(c.getY() + newY - oldY);
		    	
		    	c.setPointY(Unzoom(c.getY() + newY - oldY));
		    }
		}
		
		zone.setInitialPointX(Unzoom(newX));
		zone.setInitialPointY(Unzoom(newY));
		
		//Unzoom x and y
		//int x = Unzoom(zoomedX);
		//int y = Unzoom(zoomedY);
		//		
		//zone.setInitialPointX(x - (Unzoom(zone.getWidth()) / 2));
		//zone.setInitialPointY(y - (Unzoom(zone.getHeight()) / 2));
	}
	
	//Adds a crop
	public CropGraphic AddCrop(int id, int numPlants, int zoomedX, int zoomedY, PlantEntity p, ZoneGraphic z)
	{
		//Unzoom x and y
		int x = Unzoom(zoomedX);
		int y = Unzoom(zoomedY);
		
		//Create crop
		CropGraphic c = new CropGraphic(id, numPlants, x, y, p, z, this);
		c.addMouseDownHandler(new GraphicObjectMouseDownHandler());
		c.addMouseUpHandler(new GraphicObjectMouseUpHandler());
		c.addMouseMoveHandler(new GraphicObjectMouseMoveHandler());
		z.getCrops().add(c);
		c.Zoom(m_zoom);
		
		if (!c.getIsDeleted())
		{
			this.add(c);
		}
		
		return c;
	}
	
	//Adds a crop
	public ZoneGraphic AddZone(int id, String name, String description, int zoomedX, int zoomedY, int heigh, int width, int depth, ZoneTypeEntity t, GardenGraphic g)
	{
		//Unzoom x and y
		int x = Unzoom(zoomedX);
		int y = Unzoom(zoomedY);
		
		//Create zone
		ZoneGraphic z = new ZoneGraphic(-1, name, description, x, y, heigh, width, depth, t, g);
		z.addMouseDownHandler(new GraphicObjectMouseDownHandler());
		z.addMouseUpHandler(new GraphicObjectMouseUpHandler());
		z.addMouseMoveHandler(new GraphicObjectMouseMoveHandler());
		g.getZones().add(z);
		z.Zoom(m_zoom);
		
		if (!z.getIsDeleted())
		{
			this.add(z);
		}
		
		return z;
	}
	
	//Deletes a crop
	public void DeleteCrop(CropGraphic c)
	{
		c.setDeleted(true);
		this.remove(c);
		
		//Save the crop to delete from DB
		if (!c.getIsNew())
		{
			m_CropsDeleted.add(c);
		}
	}
	
	//Deletes a zone
	public void DeleteZone(ZoneGraphic z)
	{
		//Deletes all zone crops
		for(int i=0; i<z.m_crops.size(); i++)
		{
			DeleteCrop(z.m_crops.get(i));
		}
		
		//Deletes the zone
		z.setDeleted(true);
		this.remove(z);
		
		//Save the zone to delete from DB
		if (!z.getIsNew())
		{
			m_ZonesDeleted.add(z);
		}
	}
	
	//Adds a crop
	public void CreateZonesAndCrops(JsArray<PlantEntity> plants)
	{
		//Loads zones and crops to the garden
		for(int i=0; i<m_EntityZones.length(); i++)
		{
			ZoneTypeEntity zoneTypeEntity = null;
			for(int izte=0; izte<GardenDesigner.m_ZoneTypes.length(); izte++)
			{
				if (GardenDesigner.m_ZoneTypes.get(izte).getId() == m_EntityZones.get(i).getId())
				{
					zoneTypeEntity = GardenDesigner.m_ZoneTypes.get(izte);
					break;
				}
			}
			
			if (zoneTypeEntity == null)
			{
				Window.alert("Error loading zones: zone type " + Integer.toString(m_EntityZones.get(i).getId()) + " not found");
				return;
			}
			
			ZoneGraphic z = new ZoneGraphic(m_EntityZones.get(i), zoneTypeEntity, this);
			z.addMouseDownHandler(new GraphicObjectMouseDownHandler());
			z.addMouseUpHandler(new GraphicObjectMouseUpHandler());
			z.addMouseMoveHandler(new GraphicObjectMouseMoveHandler());
			if (!z.getIsDeleted())
			{
				this.add(z);
			}
			
			for(int j=0; j<m_EntityCrops.length(); j++)
			{
				if (m_EntityCrops.get(j).getZoneId() == m_EntityZones.get(i).getId())
				{
					//Search the plant of the crop
					for(int k=0; k<plants.length(); k++)
					{
						if (plants.get(k).getId() == m_EntityCrops.get(j).getPlantId())
						{
							//Create a CropGraphic and add to the graphic zone
							CropGraphic c = new CropGraphic(m_EntityCrops.get(j), plants.get(k), z, this);
							//CropGraphic c = new CropGraphic(m_EntityCrops.get(j).getId(), m_EntityCrops.get(j).getNumPlants(), m_EntityCrops.get(j).getPointX(), m_EntityCrops.get(j).getPointY(), plants.get(k), z, this);
							c.addMouseDownHandler(new GraphicObjectMouseDownHandler());
							c.addMouseUpHandler(new GraphicObjectMouseUpHandler());
							c.addMouseMoveHandler(new GraphicObjectMouseMoveHandler());
							z.getCrops().add(c);
							if (!c.getIsDeleted())
							{
								this.add(c);
							}
							break;
						}
					}
				}
			}
			
			this.getZones().add(z);
		}
	}
	
	public void Save(String urlToSave)
	{
		/*
		String url = "http://localhost/";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

		try {
		  Request request = builder.sendRequest(null, new RequestCallback() {
		    public void onError(Request request, Throwable exception) {
		       // Couldn't connect to server (could be timeout, SOP violation, etc.)
		    	Window.alert("Couldn't connect to serve " + exception.toString());
		    }
		    public void onResponseReceived(Request request, Response response) {
		      if (200 == response.getStatusCode()) {
		          // Process the response in response.getText()
		    	  Window.alert("Successfully " + response.getText());
		      } else {
		        // Handle the error.  Can get the status text from response.getStatusText()
		    	  Window.alert("Error " + response.getStatusText() + " code=" + Integer.toString(response.getStatusCode()));
		      }
		    }
		  });
		} catch (RequestException e) {
		  // Couldn't connect to server
		}
		return;
		*/
		
		
		
		// Construct the JSON data to send to the server
		String jsonGarden = this.toJsonString();
		String jsonZones = "[";
		String jsonCrops = "[";
		for(int i=0; i<this.getZones().size(); i++)
		{
			ZoneGraphic z = this.getZones().get(i);
			jsonZones += z.toJsonString() + ",";
			
			for(int j=0; j<z.getCrops().size(); j++)
			{
				CropGraphic c = z.getCrops().get(j);
				jsonCrops += c.toJsonString() + ",";
			}
		}
		if (jsonZones.endsWith(",")) jsonZones = jsonZones.substring(0, jsonZones.length() -1);
		jsonZones += "]";
		if (jsonCrops.endsWith(",")) jsonCrops = jsonCrops.substring(0, jsonCrops.length() -1);
		jsonCrops += "]";
		
		String postData = URL.encode("[" + jsonGarden + "," + jsonZones + "," + jsonCrops + "]");
		postData = postData.replaceAll(",", "%2C");
		
		//Window.alert("Post data: garden=" + postData);
		//Window.alert(postData);
		
	    try {
			// Send the POST to save the garden changes		
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, urlToSave);
			builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
			// wait 15000 milliseconds for the request to complete
			builder.setTimeoutMillis(15000);
	    	
			//RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost/");
			
	    	//Request response = builder.sendRequest("garden=" + postData, new RequestCallback() {
			Request response = builder.sendRequest(postData, new RequestCallback() {
	    		public void onError(Request request, Throwable exception) {
	    			if (exception instanceof RequestTimeoutException) {
	    				// handle a request timeout
	    				Window.alert("request timeout: " + exception.getMessage());
	    			} else {
	    				// handle other request errors
	    				Window.alert("error: " + exception.getMessage());
	    			}
	    		}

	    		@Override
	    		public void onResponseReceived(Request request, Response response) {
	    			// If its 200 (OK) don't show any message 
	    			if (response.getStatusCode() == 200)
	    			{
	    				Window.alert("Garden saved");
	    			}
	    			else
	    			{
	    				Window.alert("response received: " + response.getStatusText() + ", status code=" + Integer.toString(response.getStatusCode()) + " Data:" + response.getText());
	    			}
	    		}
	    	});
	    } catch (RequestException e) {
	    	Window.alert("Failed to send the request: " + e.getMessage());
	    }
	}
	
	public void ZoomToFit(int width, int height)
	{
		int zoom = 0;
				
		if (getEntity().getWidth() > width)
		{
			if (width != 0)
			{
				zoom = (getEntity().getWidth() / width) + 1;
				zoom *= -1;
			}
			else
			{
				zoom = 1;
			}
		}
		else
		{
			if (getEntity().getWidth() != 0)
			{
				zoom = (width / getEntity().getWidth()) + 1;
			}
			else
			{
				zoom = 1;
			}
		}
		
		Window.alert("Size: " + Integer.toString(width) + ", " + Integer.toString(height) + " Zoom: " + Integer.toString(zoom));
		m_zoom = zoom;
		ReZoom(0);	
	}
		
	@Override 
	public String toString()
	{
		return "[Garden (" + Integer.toString(m_entity.getWidth()) + "x" + Integer.toString(m_entity.getHeight()) + ")]";
	}
	
	public String toJsonString()
	{
		return m_entity.toJsonString();
	}
}