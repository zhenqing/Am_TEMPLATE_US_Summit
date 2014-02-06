/******************************************************************************* 
 *  Copyright 2008-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 * 
 *  Marketplace Web Service Products Java Library
 *  API Version: 2011-10-01
 * 
 */

package com.amazonservices.mws.products.my;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonservices.mws.products.MarketplaceWebServiceProducts;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsClient;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsException;
import com.amazonservices.mws.products.model.ASINListType;
//import com.amazonservices.mws.products.*;
import com.amazonservices.mws.products.model.GetLowestOfferListingsForASINRequest;
import com.amazonservices.mws.products.model.GetLowestOfferListingsForASINResponse;
import com.amazonservices.mws.products.model.GetLowestOfferListingsForASINResult;
import com.amazonservices.mws.products.model.LowestOfferListingList;
import com.amazonservices.mws.products.model.LowestOfferListingType;
import com.amazonservices.mws.products.model.MoneyType;
import com.amazonservices.mws.products.model.PriceType;
import com.amazonservices.mws.products.model.Product;
import com.amazonservices.mws.products.model.QualifiersType;
import com.amazonservices.mws.products.model.ShippingTimeType;
import com.eb.configure.ProductsConfig_MARKET_FROM;
import com.eb.configure.config_marketplace;
import com.eb.data.amazonobject.Condition;
import com.eb.data.database.business.database_indexposition_business;
import com.eb.data.database.business.database_inventory_business;
import com.eb.data.dataobject.Item_InventoryExtension2;

/**
 *
 * Get Lowest Offer Listings For ASIN  Samples
 *
 *
 */

public class GetSummitListingsForISBN {

    /**
     * Just add few required parameters, and try the service
     * Get Lowest Offer Listings For ASIN functionality
     *
     * @param args unused
     */
	
//  private static List<LowestOffer> Costs = new ArrayList<LowestOffer>();
//	private double lowestprice = 0;
//	private double chengbenjia = 0;
	
	private static Condition myusedcon;
	private static Condition mynewcon;
	public static database_inventory_business my_db;
	private static database_indexposition_business my_db_position;
	// private static int position;
	private final static String dbpositionname = config_marketplace.indexposition;
	private static final int DATABASE_STEP = config_marketplace.DATABASE_STEP;
	private static List<Item_InventoryExtension2> booklist;
	
	static{
		
		myusedcon = new Condition();
		myusedcon.condition = "used";
		myusedcon.subcondition = "good";
		
		mynewcon = new Condition();
		mynewcon.condition = "new";
		mynewcon.subcondition = "new";
		
	}
	 public static void sendMail(String content,String toAddress,String subject) throws InterruptedException{
	    	final String username = "ebzhenqing@gmail.com";
			final String password = "zhen2410144";
	 
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
	 
			Session session = Session.getInstance(props,
			  new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			  });
			try {
	 
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress("ebzhenqing@gmail.com"));
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(toAddress));
				message.setSubject(subject);
				message.setText(content);
				Transport.send(message);
				System.out.println("Done");
	 
			} catch (MessagingException e) {
				Thread.sleep(30 * 1000);
				sendMail(content,toAddress,subject);
				throw new RuntimeException(e);
				
			}
	    }
	
    public static void main(String... args) throws InterruptedException  {

    	try{
    	my_db = new database_inventory_business();
    	my_db_position = new database_indexposition_business();
    	
         // @TODO: set request parameters here
 		int  totalproducts = my_db.getMaxId();
		
		// each time deal with 10,000 products

		int nowindex = my_db_position.getValueFromName(dbpositionname);
		long time = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(time);
		my_db_position.insertStartPoint(dbpositionname,nowindex,timestamp);
		if ( nowindex == totalproducts )
		{
			nowindex = 1;
		}
		
		//position = nowindex;
		
		
			while ( nowindex < totalproducts )
			{
				
				if (nowindex + DATABASE_STEP < totalproducts )
				{
					gaijiafunc(nowindex, nowindex + DATABASE_STEP -1);
					nowindex += DATABASE_STEP;
					my_db_position.update(dbpositionname, nowindex);
				}
				else
				{
					gaijiafunc(nowindex, totalproducts);
					nowindex = totalproducts;
					my_db_position.update(dbpositionname, nowindex);
				}
	
				totalproducts = my_db.getMaxId();
			}
			nowindex = 1;
			}catch(Exception e){
				sendMail(e.getMessage().toString(),"zhaozhenqing@gmail.com","29 Summit Exception");
				e.printStackTrace();
			}finally{
				try {
					my_db.closecon();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	
    }
       
    public static void  gaijiafunc(int startpoint, int endpoint ) throws IOException, SQLException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException
   	{
   		// get gaijia products
   		
   		booklist = my_db.getBookListFromIdArrange( startpoint, endpoint );

   		// delete the duplicated asin.
   		
   		Map<String, Item_InventoryExtension2> gaijialist_temp = new HashMap<String, Item_InventoryExtension2>();
   		
   		for ( int k = 0; k < booklist.size(); k++ )
   		{
   			if ( !gaijialist_temp.containsKey(booklist.get(k).isbn) )
   			{
   				gaijialist_temp.put(booklist.get(k).isbn, booklist.get(k));
   			}
   			else
   			{
   				booklist.remove(k);
   				k--;
   			}
   			//System.out.println(booklist.get(k).isbn);
   		}
   		
   		// if no order, return.
   		if (booklist.size() == 0) 
   		{	
   			return; 
   		}
   		
   		
   		getchengbenjia(booklist,myusedcon,"ISBN");    
   		
     
   	}
    public static void getchengbenjia(List<Item_InventoryExtension2> aplist,Condition mycon, String p_function) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException
    {

    	String asinString = "";

    	if (p_function.equalsIgnoreCase("ISBN"))
    	{
	    	for (int i = 0; i < aplist.size(); i ++)
	    	{
	    		if (i !=  aplist.size() - 1)
	    		{
	    			asinString += aplist.get(i).isbn + ",";
	    		}
	    		else
	    		{
	    			asinString += aplist.get(i).isbn;
	    		}
	    	}
    	}
    	 ArrayList<ArrayList<LowestOffer>>	myCosts = getCosts(asinString, myusedcon);
    	   int retry_time = 0;
       	while ( myCosts.size()!= aplist.size() )
       	{
       		// some unexpected error happened.
       		if (retry_time > 3)
       		{
       			break;
       		}
       		retry_time ++;
       		System.out.println("sleep 30 seconds");
       		//Thread.sleep(30 * 1000);
       		myCosts = getCosts(asinString, myusedcon);
       		
       	}
    	for (int k = 0; k < aplist.size(); k++ )
			{  
    			
    			//System.out.println ("ISBN: " + asinString );
    			Regulation.checkSummit(myCosts.get(k),aplist.get(k).isbn);   
		}
    }
    
    public static ArrayList<ArrayList<LowestOffer>> getCosts( String asinString, Condition mycon ) throws InterruptedException
    {
    	 MarketplaceWebServiceProducts service = null;

         service = new MarketplaceWebServiceProductsClient(
        		ProductsConfig_MARKET_FROM.accessKeyId, 
        		ProductsConfig_MARKET_FROM.secretAccessKey, 
        		ProductsConfig_MARKET_FROM.applicationName, 
        		ProductsConfig_MARKET_FROM.applicationVersion, 
        		ProductsConfig_MARKET_FROM.config);
     
        /************************************************************************
         * Uncomment to try out Mock Service that simulates Marketplace Web Service Products 
         * responses without calling Marketplace Web Service Products  service.
         *
         * Responses are loaded from local XML files. You can tweak XML files to
         * experiment with various outputs during development
         *
         * XML files available under com/amazonservices/mws/products/mock tree
         *
         ***********************************************************************/
         //  MarketplaceWebServiceProducts service = new MarketplaceWebServiceProductsMock();

        /************************************************************************
         * Setup request parameters and uncomment invoke to try out 
         * sample for Get Lowest Offer Listings For ASIN 
         ***********************************************************************/
         
         GetLowestOfferListingsForASINRequest request = new GetLowestOfferListingsForASINRequest();
         

         request.setSellerId(ProductsConfig_MARKET_FROM.sellerId);
             
         request.setMarketplaceId(ProductsConfig_MARKET_FROM.marketplaceId);
         
         // set Asin List 
        // asinString =  "019514354X";  // "B001DCVAA8";
         
         String asinArray[]=asinString.split(",");  
         
         List<String> ListAsin = new ArrayList<String>();
         
         for (int i = 0; i < asinArray.length; i++ )
         {
        	 ListAsin.add(asinArray[i]);
         }

         ASINListType asin_list = new ASINListType();
         // Not sure if at this point you need this ..
         asin_list.setASIN(ListAsin);

         request.setASINList(asin_list);
         
         if ( (mycon.getcondition()).equalsIgnoreCase("New") )
         {
        	   request.setItemCondition("New");
         }
         else
         {
        	request.setItemCondition("Any");
         }
         
         request.setExcludeMe( true );
         
         // @TODO: set request parameters here
         
         return invokeGetLowestOfferListingsForASIN(service, request);

    }
                  
    /**
     * Get Lowest Offer Listings For ASIN  request sample
     * Gets some of the lowest prices based on the product identified by the given
     * MarketplaceId and ASIN.
     *   
     * @param service instance of MarketplaceWebServiceProducts service
     * @param request Action to invoke
     * @throws InterruptedException 
     */
    
    public static ArrayList<ArrayList<LowestOffer>> invokeGetLowestOfferListingsForASIN(MarketplaceWebServiceProducts service, GetLowestOfferListingsForASINRequest request) throws InterruptedException {
    	int retrytimes = 0;
    	while(true)
    	{
    	  	
    	ArrayList<ArrayList<LowestOffer>> als = new ArrayList<ArrayList<LowestOffer>> (); 
   
    	try {
            
            GetLowestOfferListingsForASINResponse response = service.getLowestOfferListingsForASIN(request);
           
            // System.out.println ("GetLowestOfferListingsForASIN Action Response");
                        
            java.util.List<GetLowestOfferListingsForASINResult> getLowestOfferListingsForASINResultList = response.getGetLowestOfferListingsForASINResult();
            
            for (GetLowestOfferListingsForASINResult getLowestOfferListingsForASINResult : getLowestOfferListingsForASINResultList) {

            	ArrayList<LowestOffer> a1 = new ArrayList<LowestOffer>();
            	
                if (getLowestOfferListingsForASINResult.isSetProduct()) {
               
                    Product  product = getLowestOfferListingsForASINResult.getProduct();
   
                    if (product.isSetLowestOfferListings()) {
                                            	                    	
                        LowestOfferListingList  lowestOfferListings = product.getLowestOfferListings();
                        java.util.List<LowestOfferListingType> lowestOfferListingList = lowestOfferListings.getLowestOfferListing();
                        // System.out.println("=============================================================");
                        LowestOffer cost = new LowestOffer();                     
                        for (LowestOfferListingType lowestOfferListing : lowestOfferListingList) {
                        	
                        	cost = new LowestOffer();
                        	
                        	// System.out.println("-------------------------------------------------------");
                            if (lowestOfferListing.isSetQualifiers()) {

                                QualifiersType  qualifiers = lowestOfferListing.getQualifiers();
                                if (qualifiers.isSetItemCondition()) {
                                    // System.out.println("                            ItemCondition");
                                    // System.out.println();
                                    // System.out.println("                                " + qualifiers.getItemCondition());
                                    cost.setItemCondition(qualifiers.getItemCondition());
                                    // System.out.println();
                                }
                                if (qualifiers.isSetItemSubcondition()) {
                                    // System.out.println("                            ItemSubcondition");
                                    // System.out.println();
                                    // System.out.println("                                " + qualifiers.getItemSubcondition());
                                    cost.setItemSubcondition(qualifiers.getItemSubcondition());
                                    // System.out.println();
                                }  
                                if (qualifiers.isSetFulfillmentChannel()) {
                                    // System.out.println("                            FulfillmentChannel");
                                    // System.out.println();
                                    // System.out.println("                                " + qualifiers.getFulfillmentChannel());
                                    // System.out.println();
                                    cost.setChannel(qualifiers.getFulfillmentChannel());
                                }
                                if (qualifiers.isSetShipsDomestically()) {
                                    // System.out.println("                            ShipsDomestically");
                                    // System.out.println();
                                    // System.out.println("                                " + qualifiers.getShipsDomestically());
                                    // System.out.println();
                                }
                                /*
                                else
                                {
                                	// ship internationally
                                	cost.setinternation(true);                               	
                                }
                                */
                                if (qualifiers.isSetShippingTime()) {
                                    // System.out.println("                            ShippingTime");
                                    // System.out.println();
                                    ShippingTimeType  shippingTime = qualifiers.getShippingTime();
                                    if (shippingTime.isSetMax()) {
                                        // System.out.println("                                Max");
                                        // System.out.println();
                                        // System.out.println("                                    " + shippingTime.getMax());
                                        cost.setAPshipday( shippingTime.getMax());
                                        // System.out.println();
                                    }
                                } 
                                if (qualifiers.isSetSellerPositiveFeedbackRating()) {
                                    // System.out.println("                            SellerPositiveFeedbackRating");
                                    // System.out.println();
                                    // System.out.println("                                " + qualifiers.getSellerPositiveFeedbackRating());
                                    cost.setSellerPositiveFeedbackRating(qualifiers.getSellerPositiveFeedbackRating());
                                    // System.out.println();
                                }
                            } 
                            if (lowestOfferListing.isSetNumberOfOfferListingsConsidered()) {
                                // System.out.println("                        NumberOfOfferListingsConsidered");
                                // System.out.println();
                                // System.out.println("                            " + lowestOfferListing.getNumberOfOfferListingsConsidered());
                                cost.setNumberOfListingConsidered( lowestOfferListing.getNumberOfOfferListingsConsidered() );
                                // System.out.println();
                            }
                            if (lowestOfferListing.isSetSellerFeedbackCount()) {
                                // System.out.println("                        SellerFeedbackCount");
                                // System.out.println();
                                // System.out.println("                            " + lowestOfferListing.getSellerFeedbackCount());
                                cost.setSellerFeedbackCount(  lowestOfferListing.getSellerFeedbackCount()  );
                                // System.out.println();
                            }
                            if (lowestOfferListing.isSetPrice()) {
                                // System.out.println("                        Price");
                                // System.out.println();
                                PriceType  price1 = lowestOfferListing.getPrice();
                                if (price1.isSetLandedPrice()) {
                                 //   System.out.println("                            LandedPrice");
                                 //   System.out.println();
                                    MoneyType  landedPrice1 = price1.getLandedPrice();
                                    if (landedPrice1.isSetCurrencyCode()) {
                                  //      System.out.println("                                CurrencyCode");
                                   //     System.out.println();
                                   //     System.out.println("                                    " + landedPrice1.getCurrencyCode());
                                   //     System.out.println();
                                    }
                                    if (landedPrice1.isSetAmount()) {
                                    //    System.out.println("                                Amount");
                                    //    System.out.println();
                                    //    System.out.println("                                    " + landedPrice1.getAmount());
                                        cost.setLandPrice( landedPrice1.getAmount() );
                                    //    System.out.println();
                                    }
                                } 
                                if (price1.isSetListingPrice()) {
                               //     System.out.println("                            ListingPrice");
                               //     System.out.println();
                                    MoneyType  listingPrice1 = price1.getListingPrice();
                                    if (listingPrice1.isSetCurrencyCode()) {
                                //        System.out.println("                                CurrencyCode");
                                //        System.out.println();
                                 //       System.out.println("                                    " + listingPrice1.getCurrencyCode());
                                        cost.setCurrencyCode( listingPrice1.getCurrencyCode() );
                                //        System.out.println();
                                    }
                                    if (listingPrice1.isSetAmount()) {
                                //        System.out.println("                                Amount");
                                //        System.out.println();
                                //        System.out.println("                                    " + listingPrice1.getAmount());
                                        cost.setListingPrice(  listingPrice1.getAmount() );
                                 //       System.out.println();
                                    }
                                } 
                                if (price1.isSetShipping()) {
                                //    System.out.println("                            Shipping");
                                //    System.out.println();
                                    MoneyType  shipping1 = price1.getShipping();
                                    if (shipping1.isSetCurrencyCode()) {
                                 //       System.out.println("                                CurrencyCode");
                                 //       System.out.println();
                                 //       System.out.println("                                    " + shipping1.getCurrencyCode());
                                 //       System.out.println();
                                    }
                                    if (shipping1.isSetAmount()) {
                                 //       System.out.println("                                Amount");
                                 //       System.out.println();
                                 //       System.out.println("                                    " + shipping1.getAmount());
                                 //       System.out.println();
                                    }
                                } 
                            }  
                            if (lowestOfferListing.isSetMultipleOffersAtLowestPrice()) {
                            //    System.out.println("                        MultipleOffersAtLowestPrice");
                            //    System.out.println();
                            //    System.out.println("                            " + lowestOfferListing.getMultipleOffersAtLowestPrice());
                            //    System.out.println();
                            }
                            a1.add(cost);
                        //  System.out.println("ccccccccccccccccccccccccccccccccccccccccccccccccccccc" + "add to costs ");
                        }
                        als.add(a1);
                    }                   
                    if (product.isSetOffers()) {

                    } 
                } 
                
            }
           
        } catch (MarketplaceWebServiceProductsException ex) {
            
            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.println("XML: " + ex.getXML());
            System.out.print("ResponseHeaderMetadata: " + ex.getResponseHeaderMetadata());
            
            // sleep 10 minutes
            System.out.println("Sleep 10 minutes");
            Thread.sleep( 2 * 60 * 1000 );  
            retrytimes++;
            if (retrytimes < 3)
            {
            	continue;
            }
        }
    	
    	return als;
    	
    	}   // end while loop
    	
    	
    }
                                            
}
