<?php
/****************************************
*		Server of Android IM Application
*
*		Author: ahmet oguz mermerkaya
* 	Email: ahmetmermerkaya@hotmail.com
* 	Date: Dec, 4, 2008   	
* 	
*		Supported actions: 
*			1.  authenticateUser
*			    if user is authentiated return friend list
* 		    
*			2.  signUpUser
* 		
*			3.  addNewFriend
* 		
* 		4.  responseOfFriendReqs
*************************************/


//TODO:  show error off

require_once("mysql.class.php");

$dbHost = "localhost";
$dbUsername = "root";
$dbPassword = "";
$dbName = "messenger";


$db = new MySQL($dbHost,$dbUsername,$dbPassword,$dbName);

// if operation is failed by unknown reason
define("FAILED", 0);

define("SUCCESSFUL", 1);
// when  signing up, if username is already taken, return this error
define("SIGN_UP_USERNAME_CRASHED", 2);  
// when add new friend request, if friend is not found, return this error 
define("ADD_NEW_USERNAME_NOT_FOUND", 2);

//if stranger name is not unique
define("SIGN_UP_STRANGERNAME_CRASHED", 3);  

// TIME_INTERVAL_FOR_USER_STATUS: if last authentication time of user is older 
// than NOW - TIME_INTERVAL_FOR_USER_STATUS, then user is considered offline
define("TIME_INTERVAL_FOR_USER_STATUS", 60);

define("USER_APPROVED", 1);
define("USER_UNAPPROVED", 0);


$username = (isset($_REQUEST['username']) && count($_REQUEST['username']) > 0) 
							? $_REQUEST['username'] 
							: NULL;
$password = isset($_REQUEST['password']) ? md5($_REQUEST['password']) : NULL;
$port = isset($_REQUEST['port']) ? $_REQUEST['port'] : NULL;

$action = isset($_REQUEST['action']) ? $_REQUEST['action'] : NULL;

//$guest_id= isset($_REQUEST['id']) ? $_REQUEST['id'] : NULL;

//$type= isset($_REQUEST['type']) ? $_REQUEST['type'] : NULL;

if (($username == NULL || $password == NULL))	 
{
	echo FAILED;
	exit;
}

$out = NULL;

error_log($action."\r\n", 3, "error.log");

	function GenerateRandID()
	{
		$rand = md5(GenerateRandSTR(16)+(string)time());
			return $rand;
	}
	
	/* GenerateRandSTR - function to generate a string of a specified
		length made of random letters(lower and uppercase) and digits */
	function GenerateRandSTR($length)
	{
		$randstr = "";
			for($i=0;$i<$length;$i++)
			{
				$randnum = mt_rand(0,61);
				if($randnum < 10)
				{
					$randstr .= chr($randnum + 48);
				}
				else
				{
					if($randnum < 36)
					{
						$randstr .= chr($randnum + 55);
					}
					else
					{
						$randstr .= chr($randnum + 61);
					}
				}
			}
		return $randstr;
	}

	
	if($action=="authenticateUser"){
		if ($port != NULL  && ($userId = authenticateUser($db, $username, $password, $port)) != NULL) 
		{					
			
			// providerId and requestId is Id of  a friend pair,
			// providerId is the Id of making first friend request
			// requestId is the Id of the friend approved the friend request made by providerId
			
			// fetching friends, 
			// left join expression is a bit different, 
			//		it is required to fetch the friend, not the users itself
			
		/*	$sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP, 
										f.providerId, f.requestId, f.status, u.port 
							from friends f
							left join users u on 
										u.Id = if ( f.providerId = ".$userId.", f.requestId, f.providerId ) 
							where (f.providerId = ".$userId." and f.status=".USER_APPROVED.")  or 
										 f.requestId = ".$userId." ";
			*/							 
			$sql = "select u.Id, u.username, u.status as onlinestatus, u.IP, 
										f.providerId, f.requestId, f.status, u.port 
							from friends f
							left join users u on 
										u.Id = if ( f.providerId = ".$userId.", f.requestId, f.providerId ) 
							where (f.providerId = ".$userId." and f.status=".USER_APPROVED.")  or 
										 f.requestId = ".$userId." ";
			/*
			$sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP, u.port, u.status from users u 
			WHERE u.username<>'".$username."'" ;
			*/
		//	$sql="SELECT * FROM users";
	
			if ($result = $db->query($sql))			
			{
					$out .= "<data>"; 
					$out .= "<user userKey='".$userId."' />";
					while ($row = $db->fetchObject($result))
					{
						$status = "offline";
						if (((int)$row->status) == USER_UNAPPROVED)
						{		
							$status = "unApproved";
						}
					/*	else if (((int)$row->authenticateTimeDifference) < TIME_INTERVAL_FOR_USER_STATUS)
						{
							$status = "online";
							 
						}*/
						else if ((int)$row->onlinestatus==1)//make it online if u.status=1
						{
							$status = "online";
							 
						}
						$out .= "<friend  username = '".$row->username."'  status='".$status."' IP='".$row->IP."' 
												userKey = '".$row->Id."'  port='".$row->port."'/>";
												
												// to increase security, we need to change userKey periodically and pay more attention
												// receiving message and sending message 
						
					}				
					$out .= "</data>";
			}
			else
			{
				$out = FAILED;
			}			
		}
		else
		{
				// exit application if not authenticated user
				$out = FAILED;
		}
	}
	else if($action=="strangerauthenticateUser"){
	
		$displayname=$_REQUEST['displayname'];
	
		if ($port != NULL  && ($userId = authenticateUser($db, $username, $password, $port)) != NULL) 
		{		

			$sql1="update users set strangername='".$displayname."' where username='".$username."'";
	
			if ($result = $db->query($sql1))			
			{
			/*	$sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP, u.port, u.status from users u 
				WHERE u.status=1 AND u.username<>'".$username."' ORDER BY rand() LIMIT 50 " ;*/

				$sql = "select u.Id, u.username, u.status as onlinestatus, u.IP, u.port, u.status, u.strangername from users u 
				WHERE u.status=1 AND u.username<>'".$username."' ORDER BY rand() LIMIT 50 " ;
				
			//	$sql="SELECT * FROM users";
		
				if ($result = $db->query($sql))			
				{
						$out .= "<data>"; 
						$out .= "<user userKey='".$userId."' />";
						while ($row = $db->fetchObject($result))
						{
							$status = "offline";
							if ((int)$row->onlinestatus==1)
							{
								$status = "online";
							}
							$out .= "<stranger  username = '".$row->username."'  status='".$status."' IP='".$row->IP."' 
													userKey = '".$row->Id."' displayName='".$row->strangername."'  port='".$row->port."'/>";
													
													// to increase security, we need to change userKey periodically and pay more attention
													// receiving message and sending message 
							
						}				
						$out .= "</data>";
				}
				else
				{
					$out = FAILED;
				}
				
			}
			else
			{
				$out = FAILED;
			}
		}
		else
		{
				// exit application if not authenticated user
				$out = FAILED;
		}
	}
	else if($action=="LogoutUser"){
		$query="UPDATE users SET status = 0,port=0 WHERE username = '$username' and password='$password'";
		
		$db->query($query);
	
	}
	else if($action=="signUpUser"){
		if (isset($_REQUEST['email']))
		{
			$email = $_REQUEST['email'];
			$strangerName=$_REQUEST['stranger'];
			$phone=$_REQUEST['phone'];
			
			echo $phone;
			
			$sql = "select Id from  users 
			 				where username = '".$username."' limit 1";
			 
		
			 				
			if ($result = $db->query($sql))
			{
			 		if ($db->numRows($result) == 0) 
			 		{
							$sql1 = "select Id from  users 
			 				where strangername = '".$strangerName."' limit 1";
							
							if($result = $db->query($sql1)){
								if ($db->numRows($result) == 0) 
								{
									$sql = "insert into users(username, password, strangername, phone, email)
										values ('".$username."', '".$password."', '".$strangerName."', ".$phone.", '".$email."') ";		 					
													
										error_log("$sql", 3 , "error_log");
									if ($db->query($sql))	
									{
											$out = SUCCESSFUL;
									}				
									else {
											$out = FAILED;
									}
								}
								else
								{
									$out = SIGN_UP_STRANGERNAME_CRASHED;
								}
							}
							else
							{
								$out=FAILED;
							}
			 		}
			 		else
			 		{
			 			$out = SIGN_UP_USERNAME_CRASHED;
			 		}
			 }				 	 	
		}
		else
		{
			$out = FAILED;
		}	
	}
	else if($action=="addGuest"){
		$val=GenerateRandID();
		
		$sql = "INSERT INTO guests (id,timestamp,IP,port) VALUES('".$val."',".time().",'".$_SERVER["REMOTE_ADDR"]."','$port')";
				
		$db->query($sql);
		echo $val;
	}
	else if($action=="getStranger"){

		$sql="SELECT * FROM guests WHERE id<>'".$guest_id."'";
		
		$result=$db->query($sql);
		
		$max=mysql_num_rows($result);
		
		if($max==0)
			exit;
		
		$rand=mt_rand(1,$max);
		
		$i=0;
		while($i<$rand){
			$i++;
			$data=mysql_fetch_array($result);
		}		

		echo $data[$type];
		
			/*if ($result = $db->query($sql))
			{
					$out .= "<data>"; 
					while ($row = $db->fetchObject($result))
					{
						$out .= "<stranger  id = '".$row->id."' IP='".$row->IP."' port='".$row->port."'/>";						
					}				
					$out .= "</data>";
			}
			else
			{
				$out = FAILED;
			}*/
			
//		$sql = "INSERT INTO guests (id,timestamp,IP,port) VALUES('".$val."',".time().",'".$_SERVER["REMOTE_ADDR"]."','$port')";
				
//		$db->query($sql);
	}	
	else if($action=="addNewFriend"){
		$userId = authenticateUserNew($db, $username, $password);
		if ($userId != NULL)
		{
			
			if (isset($_REQUEST['friendUserName']))			
			{				
				 $friendUserName = $_REQUEST['friendUserName'];
				 
				 $sql = "select Id from users 
				 				 where username='".$friendUserName."' 
				 				 limit 1";
				 if ($result = $db->query($sql))
				 {
				 		if ($row = $db->fetchObject($result))
				 		{
				 			 $requestId = $row->Id;
				 			 
				 			 if ($row->Id != $userId)
				 			 {
				 			 		 $sql = "insert into friends(providerId, requestId, status)
				 				  		 values(".$userId.", ".$requestId.", ".USER_UNAPPROVED.")";
							 
									 if ($db->query($sql))
									 {
									 		$out = SUCCESSFUL;
									 }
									 else
									 {
									 		$out = FAILED;
									 }
							}
							else
							{
								$out = FAILED;  // user add itself as a friend
							} 		 				 				  		 
				 		}
				 		else
				 		{
				 			$out = FAILED;			 			
				 		}
				 }				 				 
				 else
				 {
				 		$out = FAILED;
				 }				
			}
			else
			{
					$out = FAILED;
			} 			
		}
		else
		{
			$out = FAILED;
		}
	}
	else if($action=="deleteFriend"){
		$userId = authenticateUserNew($db, $username, $password);
		if ($userId != NULL)
		{
			
			if (isset($_REQUEST['friendUserName']))			
			{				
				 $friendUserName = $_REQUEST['friendUserName'];
				 
				 $sql = "select Id from users 
				 				 where username='".$friendUserName."' 
				 				 limit 1";
				 if ($result = $db->query($sql))
				 {
				 		if ($row = $db->fetchObject($result))
				 		{
				 			 $requestId = $row->Id;
				 			 
				 			 if ($row->Id != $userId)
				 			 {
									$sql = "delete from friends where providerId=".$requestId." AND requestId=".$userId." AND status=1)";
							 
									 if ($db->query($sql))
									 {
									 		$out = SUCCESSFUL;
									 }
									 else
									 {
									 		$out = FAILED;
									 }
									 
									 
				 			 		 $sql = "delete from friends where providerId=".$userId." AND requestId=".$requestId." AND status=1";
							 
									 if ($db->query($sql))
									 {
									 		$out = SUCCESSFUL;
									 }
									 else
									 {
									 		$out = FAILED;
									 }
							}
							else
							{
								$out = FAILED;  // user add itself as a friend
							} 		 				 				  		 
				 		}
				 		else
				 		{
				 			$out = FAILED;			 			
				 		}
				 }				 				 
				 else
				 {
				 		$out = FAILED;
				 }				
			}
			else
			{
					$out = FAILED;
			} 			
		}
		else
		{
			$out = FAILED;
		}
	}
	else if($action=="responseOfFriendReqs"){
		$userId = authenticateUserNew($db, $username, $password);
		if ($userId != NULL)
		{
			$sqlApprove = NULL;
			$sqlDiscard = NULL;
			if (isset($_REQUEST['approvedFriends']))
			{
				  $friendNames = split(",", $_REQUEST['approvedFriends']);
				  $friendCount = count($friendNames);
				  $friendNamesQueryPart = NULL;
				  for ($i = 0; $i < $friendCount; $i++)
				  {
				  	if (strlen($friendNames[$i]) > 0)
				  	{
				  		if ($i > 0 )
				  		{
				  			$friendNamesQueryPart .= ",";
				  		}
				  		
				  		$friendNamesQueryPart .= "'".$friendNames[$i]."'";
				  		
				  	}			  	
				  	
				  }
				  if ($friendNamesQueryPart != NULL)
				  {
				  	$sqlApprove = "update friends set status = ".USER_APPROVED."
				  					where requestId = ".$userId." and 
				  								providerId in (select Id from users where username in (".$friendNamesQueryPart."));
				  				";
					
					/*
					$sql_query="select Id from users where username in (".$friendNamesQueryPart.")";
					
					if($result = $db->query($sql_query)){
						$row = $db->fetchObject($result);
						
						$sql_query1 = "insert into friends(providerId, requestId, status)
				 				  		 values(".$userId.", ".$row->Id.", ".USER_APPROVED.")";
										 
						if(!$db->query($sql_query1))
							$out=FAILED;
					}
					else
					{
						$out=FAILED;
					}*/					
				  }
				  				  
			}
			if (isset($_REQUEST['discardedFriends']))
			{
					$friendNames = split(",", $_REQUEST['discardedFriends']);
				  $friendCount = count($friendNames);
				  $friendNamesQueryPart = NULL;
				  for ($i = 0; $i < $friendCount; $i++)
				  {
				  	if (strlen($friendNames[$i]) > 0)
				  	{
				  		if ($i > 0 )
				  		{
				  			$friendNamesQueryPart .= ",";
				  		}
				  		
				  		$friendNamesQueryPart .= "'".$friendNames[$i]."'";
				  		
				  	}				  	
				  }
				  if ($friendNamesQueryPart != NULL)
				  {
				  	$sqlDiscard = "delete from friends 
				  						where requestId = ".$userId." and 
				  									providerId in (select Id from users where username in (".$friendNamesQueryPart."));
				  							";
				  }						
			}
			
			if (  ($sqlApprove != NULL ? $db->query($sqlApprove) : true) &&
						($sqlDiscard != NULL ? $db->query($sqlDiscard) : true) 
			   )
			{
				$out = SUCCESSFUL;
			}
			else
			{
				$out = FAILED;
			}		
		}
		else
		{
			$out = FAILED;
		}
	}
	else if($action=="removeGuest"){
	
		$sql = "DELETE FROM guests WHERE id='".$guest_id."'";
		$db->query($sql) or die('could not query');	
	}
	else{
		$out = FAILED;		
	}

echo $out;



///////////////////////////////////////////////////////////////
function authenticateUser($db, $username, $password, $port)
{
	
	$sql = "select Id from users 
					where username = '".$username."' and password = '".$password."' 
					limit 1";
	
	$out = NULL;
	if ($result = $db->query($sql))
	{
		if ($row = $db->fetchObject($result))
		{
				$out = $row->Id;
				
				$sql = "update users set 	status=1,
											authenticationTime = NOW(), 
											IP = '".$_SERVER["REMOTE_ADDR"]."' ,
											port = ".$port." 
								where Id = ".$row->Id."
								limit 1";
				
				$db->query($sql);			
		}		
	}
	
	return $out;
}

function authenticateUserNew($db, $username, $password)
{
	
	$sql = "select Id from users 
					where username = '".$username."' and password = '".$password."' 
					limit 1";
	
	$out = NULL;
	if ($result = $db->query($sql))
	{
		if ($row = $db->fetchObject($result))
		{
				$out = $row->Id;
				
				$sql = "update users set 	status=1,
											authenticationTime = NOW(), 
											IP = '".$_SERVER["REMOTE_ADDR"]."' 
								where Id = ".$row->Id."
								limit 1";
				
				$db->query($sql);			
		}		
	}
	
	return $out;
}

?>