package com.oyunstudyosu.debug
{
   import com.junkbyte.console.Cc;
   import com.oyunstudyosu.engine.scene.components.SceneBotComponent;
   import com.oyunstudyosu.engine.scene.components.SceneCharacterComponent;
   import com.oyunstudyosu.engine.scene.components.SceneDoorComponent;
   import flash.display.Bitmap;
   import flash.display.BitmapData;
   import flash.display.DisplayObject;
   import flash.display.DisplayObjectContainer;
   import flash.display.Shape;
   import flash.display.Sprite;
   import flash.events.KeyboardEvent;
   import flash.events.MouseEvent;
   import flash.filters.DropShadowFilter;
   import flash.geom.Matrix;
   import flash.geom.Point;
   import flash.geom.Rectangle;
   import flash.system.System;
   import flash.text.TextField;
   import flash.text.TextFieldAutoSize;
   import flash.text.TextFormat;
   
   public class DebugConsole
   {
      
      private static var _root:DisplayObjectContainer;
      
      private static var _mainPanel:Sprite;
      
      private static var _consoleLogPanel:Sprite;
      
      private static var _realObjects:Array = [];
      
      private static var _selectedRealObject:Object = null;
      
      private static var _highlightSprite:Sprite = null;
      
      private static var _consoleLogs:Array = [];
      
      private static var _maxConsoleLogs:int = 100;
      
      private static var _inited:Boolean = false;
      
      private static var _isPanelVisible:Boolean = false;
      
      private static var _directManipulationMode:String = "select";
      
      private static var _showHighlights:Boolean = true;
      
      private static var _isDraggingObject:Boolean = false;
      
      private static var _dragOffset:Point = new Point();
      
      private static var _propertiesPanel:Sprite = null;
      
      private static var _isDraggingPanel:Boolean = false;
      
      private static var _panelDragStart:Point = new Point();
      
      private static var _panelStartPos:Point = new Point();
      
      private static var _isDraggingFromPanel:Boolean = false;
      
      private static var _draggedItemData:Object = null;
      
      private static var _dragPreview:Sprite = null;
      
      private static var _panelScale:Number = 1;
      
      private static var _minScale:Number = 0.6;
      
      private static var _maxScale:Number = 1.5;
      
      private static var _scrollPosition:Number = 0;
      
      private static var _maxScroll:Number = 0;
      
      private static var _coordsDisplay:TextField = null;
      
      private static const PANEL_BG:uint = 1710638;
      
      private static const PANEL_DARK:uint = 1450302;
      
      private static const ACCENT_COLOR:uint = 1002613;
      
      private static const HIGHLIGHT_COLOR:uint = 3310264;
      
      private static const TEXT_COLOR:uint = 12313082;
      
      private static const SUCCESS_COLOR:uint = 65280;
      
      private static const ERROR_COLOR:uint = 16739179;
      
      private static var PANEL_WIDTH:Number = 450;
      
      private static var PANEL_HEIGHT:Number = 700;
      
      private static const BASE_PANEL_WIDTH:Number = 450;
      
      private static const BASE_PANEL_HEIGHT:Number = 700;
      
      private static var _objectsListPanel:Sprite;
      
      private static var _statsText:TextField;
      
      private static var _draggerBtn:Sprite;
      
      public static var trackedPackets:Array = [];
       
      
      public function DebugConsole()
      {
         super();
      }
      
      public static function init(root:DisplayObjectContainer) : void
      {
         if(_inited || root == null)
         {
            return;
         }
         _inited = true;
         _root = root;
         trace("🎮 [ENHANCED ADMIN] Initializing...");
         initConsole();
         createMainPanel();
         setupEventListeners();
         info("SYSTEM","✅ Enhanced Admin Panel Ready! Press F12");
      }
      
      private static function initConsole() : void
      {
         var channels:Array;
         var ch:String;
         try
         {
            Cc.startOnStage(_root.stage,"~");
            Cc.visible = false;
            Cc.config.alwaysOnTop = true;
            Cc.config.maxLines = 10000;
            channels = ["SYSTEM","ROOM","BOTS","USERS","OBJECTS","COORDS","PACKETS","DRAG","ERROR"];
            for each(ch in channels)
            {
               try
               {
                  Cc.addChannel(ch);
               }
               catch(e:Error)
               {
               }
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function createMainPanel() : void
      {
         var bg:Sprite;
         var border:Sprite;
         _mainPanel = new Sprite();
         _mainPanel.name = "EnhancedAdminPanel";
         bg = new Sprite();
         bg.name = "panelBg";
         bg.graphics.beginFill(0,0.95);
         bg.graphics.drawRoundRect(0,0,PANEL_WIDTH,PANEL_HEIGHT,12,12);
         bg.graphics.endFill();
         _mainPanel.addChild(bg);
         border = new Sprite();
         border.graphics.lineStyle(3,HIGHLIGHT_COLOR,1);
         border.graphics.drawRoundRect(0,0,PANEL_WIDTH,PANEL_HEIGHT,12,12);
         _mainPanel.addChild(border);
         _mainPanel.filters = [new DropShadowFilter(25,45,0,0.9,25,25,1,3)];
         createHeader();
         createLiveCoords();
         createObjectsList();
         createConsoleLog();
         createActionButtons();
         createResizeHandle();
         positionPanel();
         try
         {
            _root.stage.addChild(_mainPanel);
            _mainPanel.visible = _isPanelVisible;
         }
         catch(e:Error)
         {
         }
      }
      
      private static function createHeader() : void
      {
         var header:Sprite = new Sprite();
         header.name = "header";
         header.y = 0;
         var bg:Shape = new Shape();
         bg.graphics.beginFill(PANEL_DARK,1);
         bg.graphics.drawRoundRect(0,0,PANEL_WIDTH,60,12,12);
         bg.graphics.endFill();
         header.addChild(bg);
         _draggerBtn = new Sprite();
         _draggerBtn.name = "dragger";
         _draggerBtn.graphics.beginFill(0,0.01);
         _draggerBtn.graphics.drawRect(0,0,PANEL_WIDTH - 100,35);
         _draggerBtn.graphics.endFill();
         _draggerBtn.buttonMode = true;
         _draggerBtn.addEventListener(MouseEvent.MOUSE_DOWN,onPanelDragStart);
         header.addChild(_draggerBtn);
         var title:TextField = createText("🎮 ADMIN PANEL [F12] - Drag Here",15,8,320,18,true);
         title.mouseEnabled = false;
         header.addChild(title);
         var scanBtn:Sprite = createSmallButton("🔍",15,35,65,20,scanRealGameObjects,HIGHLIGHT_COLOR);
         header.addChild(scanBtn);
         var refreshBtn:Sprite = createSmallButton("🔄",85,35,65,20,refreshView,ACCENT_COLOR);
         header.addChild(refreshBtn);
         var clearBtn:Sprite = createSmallButton("🧹",155,35,65,20,clearAllHighlights,ERROR_COLOR);
         header.addChild(clearBtn);
         var zoomInBtn:Sprite = createSmallButton("🔍+",PANEL_WIDTH - 130,35,35,20,zoomIn,SUCCESS_COLOR);
         header.addChild(zoomInBtn);
         var zoomOutBtn:Sprite = createSmallButton("🔍-",PANEL_WIDTH - 90,35,35,20,zoomOut,16750592);
         header.addChild(zoomOutBtn);
         var closeBtn:Sprite = createSmallButton("❌",PANEL_WIDTH - 45,8,35,35,togglePanel,ERROR_COLOR);
         header.addChild(closeBtn);
         _mainPanel.addChild(header);
      }
      
      private static function createLiveCoords() : void
      {
         _coordsDisplay = new TextField();
         _coordsDisplay.x = 10;
         _coordsDisplay.y = 68;
         _coordsDisplay.width = PANEL_WIDTH - 20;
         _coordsDisplay.height = 25;
         _coordsDisplay.selectable = false;
         _coordsDisplay.mouseEnabled = false;
         _coordsDisplay.background = true;
         _coordsDisplay.backgroundColor = 856343;
         _coordsDisplay.border = true;
         _coordsDisplay.borderColor = ACCENT_COLOR;
         var fmt:TextFormat = new TextFormat("_sans",13,65280,true);
         fmt.align = "center";
         _coordsDisplay.defaultTextFormat = fmt;
         _coordsDisplay.text = "📍 Position: --";
         _mainPanel.addChild(_coordsDisplay);
      }
      
      private static function createObjectsList() : void
      {
         _objectsListPanel = new Sprite();
         _objectsListPanel.x = 10;
         _objectsListPanel.y = 100;
         var bg:Shape = new Shape();
         bg.graphics.beginFill(1973806,0.8);
         bg.graphics.drawRoundRect(0,0,PANEL_WIDTH - 20,330,8,8);
         bg.graphics.endFill();
         _objectsListPanel.addChild(bg);
         var titleText:TextField = createText("📦 Game Objects (Scroll with Mouse Wheel)",10,8,PANEL_WIDTH - 40,14,true);
         _objectsListPanel.addChild(titleText);
         var content:Sprite = new Sprite();
         content.name = "content";
         content.x = 5;
         content.y = 35;
         _objectsListPanel.addChild(content);
         var mask:Shape = new Shape();
         mask.graphics.beginFill(16711680);
         mask.graphics.drawRect(5,35,PANEL_WIDTH - 30,290);
         mask.graphics.endFill();
         _objectsListPanel.addChild(mask);
         content.mask = mask;
         var scrollbar:Sprite = createScrollbar();
         scrollbar.x = PANEL_WIDTH - 25;
         scrollbar.y = 35;
         _objectsListPanel.addChild(scrollbar);
         _objectsListPanel.addEventListener(MouseEvent.MOUSE_WHEEL,onMouseWheel);
         _mainPanel.addChild(_objectsListPanel);
      }
      
      private static function createScrollbar() : Sprite
      {
         var scrollbar:Sprite = new Sprite();
         scrollbar.name = "scrollbar";
         var track:Shape = new Shape();
         track.graphics.beginFill(2763326,0.8);
         track.graphics.drawRoundRect(0,0,15,290,5,5);
         track.graphics.endFill();
         scrollbar.addChild(track);
         var thumb:Sprite = new Sprite();
         thumb.name = "thumb";
         thumb.graphics.beginFill(ACCENT_COLOR,0.9);
         thumb.graphics.drawRoundRect(0,0,15,50,5,5);
         thumb.graphics.endFill();
         thumb.buttonMode = true;
         scrollbar.addChild(thumb);
         return scrollbar;
      }
      
      private static function onMouseWheel(e:MouseEvent) : void
      {
         _scrollPosition -= e.delta * 30;
         if(_scrollPosition < 0)
         {
            _scrollPosition = 0;
         }
         if(_scrollPosition > _maxScroll)
         {
            _scrollPosition = _maxScroll;
         }
         updateObjectsList();
      }
      
      private static function createConsoleLog() : void
      {
         var bg:Shape;
         var title:TextField;
         var clearBtn:Sprite;
         var exportBtn:Sprite;
         var logDisplay:TextField;
         var fmt:TextFormat;
         _consoleLogPanel = new Sprite();
         _consoleLogPanel.x = 10;
         _consoleLogPanel.y = 440;
         bg = new Shape();
         bg.graphics.beginFill(856343,0.98);
         bg.graphics.drawRoundRect(0,0,PANEL_WIDTH - 20,160,8,8);
         bg.graphics.endFill();
         _consoleLogPanel.addChild(bg);
         title = createText("📋 Console Log",10,5,200,14,true);
         _consoleLogPanel.addChild(title);
         clearBtn = createSmallButton("🗑️",PANEL_WIDTH - 60,3,25,18,function():void
         {
            _consoleLogs = [];
            updateConsoleDisplay();
         },ERROR_COLOR);
         _consoleLogPanel.addChild(clearBtn);
         exportBtn = createSmallButton("📋",PANEL_WIDTH - 33,3,25,18,exportConsoleLogs,ACCENT_COLOR);
         _consoleLogPanel.addChild(exportBtn);
         logDisplay = new TextField();
         logDisplay.name = "logDisplay";
         logDisplay.x = 5;
         logDisplay.y = 28;
         logDisplay.width = PANEL_WIDTH - 30;
         logDisplay.height = 127;
         logDisplay.multiline = true;
         logDisplay.wordWrap = true;
         logDisplay.selectable = true;
         logDisplay.background = true;
         logDisplay.backgroundColor = 0;
         logDisplay.textColor = 65280;
         fmt = new TextFormat("Courier New",10,65280);
         logDisplay.defaultTextFormat = fmt;
         logDisplay.text = "Console logs...\n";
         _consoleLogPanel.addChild(logDisplay);
         _mainPanel.addChild(_consoleLogPanel);
      }
      
      private static function createActionButtons() : void
      {
         var buttons:Array;
         var btn:Object;
         var button:Sprite;
         var info:TextField;
         var y:Number = 610;
         _statsText = createText("Objects: 0 | Bots: 0 | Users: 0",15,y,PANEL_WIDTH - 30,13,false);
         _statsText.textColor = 12313082;
         _mainPanel.addChild(_statsText);
         y += 22;
         buttons = [{
            "label":"🎯 Select",
            "action":function():void
            {
               setMode("select");
            },
            "x":15,
            "w":100
         },{
            "label":"✋ Drag",
            "action":function():void
            {
               setMode("drag");
            },
            "x":125,
            "w":100
         },{
            "label":"👁️ Hide",
            "action":function():void
            {
               setMode("hide");
            },
            "x":235,
            "w":95
         },{
            "label":"🗑️ Del",
            "action":function():void
            {
               setMode("delete");
            },
            "x":340,
            "w":95
         }];
         for each(btn in buttons)
         {
            button = createSmallButton(btn.label,btn.x,y,btn.w,28,btn.action,ACCENT_COLOR);
            button.name = "mode_" + btn.label;
            _mainPanel.addChild(button);
         }
         y += 35;
         info = createText("💡 Drag panel header • Zoom • Scroll list",15,y,PANEL_WIDTH - 30,12,false);
         info.textColor = 8359053;
         _mainPanel.addChild(info);
      }
      
      private static function createResizeHandle() : void
      {
         var handle:Sprite = new Sprite();
         handle.name = "resizeHandle";
         handle.x = PANEL_WIDTH - 20;
         handle.y = PANEL_HEIGHT - 20;
         handle.graphics.beginFill(ACCENT_COLOR,0.7);
         handle.graphics.moveTo(0,20);
         handle.graphics.lineTo(20,0);
         handle.graphics.lineTo(20,20);
         handle.graphics.lineTo(0,20);
         handle.graphics.endFill();
         handle.buttonMode = true;
         handle.addEventListener(MouseEvent.MOUSE_DOWN,onResizeStart);
         _mainPanel.addChild(handle);
      }
      
      private static function onResizeStart(e:MouseEvent) : void
      {
         info("SYSTEM","Use zoom buttons to resize");
      }
      
      private static function zoomIn() : void
      {
         _panelScale += 0.1;
         if(_panelScale > _maxScale)
         {
            _panelScale = _maxScale;
         }
         applyPanelScale();
      }
      
      private static function zoomOut() : void
      {
         _panelScale -= 0.1;
         if(_panelScale < _minScale)
         {
            _panelScale = _minScale;
         }
         applyPanelScale();
      }
      
      private static function applyPanelScale() : void
      {
         _mainPanel.scaleX = _mainPanel.scaleY = _panelScale;
         info("SYSTEM","Panel scale: " + Math.round(_panelScale * 100) + "%");
      }
      
      private static function onPanelDragStart(e:MouseEvent) : void
      {
         _isDraggingPanel = true;
         _panelDragStart.x = e.stageX;
         _panelDragStart.y = e.stageY;
         _panelStartPos.x = _mainPanel.x;
         _panelStartPos.y = _mainPanel.y;
         _root.stage.addEventListener(MouseEvent.MOUSE_MOVE,onPanelDragMove);
         _root.stage.addEventListener(MouseEvent.MOUSE_UP,onPanelDragEnd);
      }
      
      private static function onPanelDragMove(e:MouseEvent) : void
      {
         if(_isDraggingPanel)
         {
            _mainPanel.x = _panelStartPos.x + (e.stageX - _panelDragStart.x);
            _mainPanel.y = _panelStartPos.y + (e.stageY - _panelDragStart.y);
         }
      }
      
      private static function onPanelDragEnd(e:MouseEvent) : void
      {
         _isDraggingPanel = false;
         _root.stage.removeEventListener(MouseEvent.MOUSE_MOVE,onPanelDragMove);
         _root.stage.removeEventListener(MouseEvent.MOUSE_UP,onPanelDragEnd);
      }
      
      private static function scanRealGameObjects() : void
      {
         var sanalika:Object;
         var scene:Object;
         info("SYSTEM","🔍 Scanning ALL objects...");
         _realObjects = [];
         _scrollPosition = 0;
         try
         {
            sanalika = getSanalikaInstance();
            if(!sanalika || !sanalika.engine || !sanalika.engine.scene)
            {
               error("SYSTEM","Cannot access engine");
               return;
            }
            scene = sanalika.engine.scene;
            scanBots(scene);
            scanUsers(scene);
            scanFurniture(scene);
            scanDoors(scene);
            scanFloors(scene);
            scanWalls(scene);
            updateObjectsList();
            updateStats();
            info("SYSTEM","✅ Found " + _realObjects.length + " objects");
         }
         catch(e:Error)
         {
            error("SYSTEM","Scan failed: " + e.message);
         }
      }
      
      private static function scanBots(scene:Object) : void
      {
         var botComponent:Object;
         var botList:Array;
         var botEntry:Object;
         var botObj:Object;
         try
         {
            botComponent = scene.getComponent(SceneBotComponent);
            if(!botComponent || !botComponent.botList)
            {
               return;
            }
            botList = botComponent.botList;
            for each(botEntry in botList)
            {
               if(botEntry)
               {
                  botObj = {
                     "obj":botEntry,
                     "clip":botEntry.clip,
                     "type":"bot",
                     "name":botEntry.name || botEntry.id || "bot",
                     "gridX":botEntry.x,
                     "gridY":botEntry.z,
                     "gridZ":0,
                     "visible":(!!botEntry.clip ? botEntry.clip.visible : false),
                     "hasClip":botEntry.clip != null,
                     "thumbnail":null
                  };
                  if(botEntry.clip)
                  {
                     botObj.thumbnail = createThumbnail(botEntry.clip);
                  }
                  _realObjects.push(botObj);
               }
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function scanUsers(scene:Object) : void
      {
         var charComponent:Object;
         var charList:Object;
         var charId:String;
         var char:Object;
         var userObj:Object;
         try
         {
            charComponent = scene.getComponent(SceneCharacterComponent);
            if(!charComponent || !charComponent.characterList)
            {
               return;
            }
            charList = charComponent.characterList;
            for(charId in charList)
            {
               char = charList[charId];
               if(char)
               {
                  userObj = {
                     "obj":char,
                     "clip":char.container,
                     "type":"user",
                     "name":char.avatarName || char.id || "user",
                     "gridX":(!!char.currentTile ? char.currentTile.x : 0),
                     "gridY":(!!char.currentTile ? char.currentTile.y : 0),
                     "gridZ":0,
                     "visible":(!!char.container ? !char.hide : false),
                     "hasClip":char.container != null,
                     "thumbnail":null
                  };
                  if(char.container)
                  {
                     userObj.thumbnail = createThumbnail(char.container);
                  }
                  _realObjects.push(userObj);
               }
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function scanFurniture(scene:Object) : void
      {
         var mapEntries:Object;
         var entry:Object;
         var furnitureObj:Object;
         try
         {
            mapEntries = scene.mapEntries;
            if(!mapEntries)
            {
               return;
            }
            info("OBJECTS","Scanning mapEntries: " + mapEntries.length);
            for each(entry in mapEntries)
            {
               if(entry)
               {
                  if(!(entry.type == "floor" || entry.type == "wall"))
                  {
                     furnitureObj = {
                        "obj":entry,
                        "clip":entry.clip,
                        "type":entry.type || "furniture",
                        "name":entry.definition || entry.id || "furniture_" + entry.x + "_" + entry.z,
                        "definition":entry.definition,
                        "gridX":entry.x,
                        "gridY":entry.z,
                        "gridZ":entry.y,
                        "width":entry.width,
                        "height":entry.depth,
                        "dir":entry.dir,
                        "visible":(!!entry.clip ? entry.clip.visible : false),
                        "hasClip":entry.clip != null,
                        "thumbnail":(!!entry.clip ? createThumbnail(entry.clip) : null)
                     };
                     _realObjects.push(furnitureObj);
                  }
               }
            }
            info("OBJECTS","Found " + _realObjects.length + " furniture items");
         }
         catch(e:Error)
         {
            error("OBJECTS","Furniture scan error: " + e.message);
         }
      }
      
      private static function scanDoors(scene:Object) : void
      {
         var doorComponent:Object;
         var doorList:Array;
         var door:Object;
         var doorObj:Object;
         try
         {
            doorComponent = scene.getComponent(SceneDoorComponent);
            if(!doorComponent || !doorComponent.doorList)
            {
               return;
            }
            doorList = doorComponent.doorList;
            info("OBJECTS","Found " + doorList.length + " doors");
            for each(door in doorList)
            {
               if(door)
               {
                  doorObj = {
                     "obj":door,
                     "clip":door,
                     "type":"door",
                     "name":door.name || "door",
                     "gridX":0,
                     "gridY":0,
                     "gridZ":0,
                     "visible":door.visible,
                     "hasClip":true,
                     "thumbnail":createThumbnail(door)
                  };
                  _realObjects.push(doorObj);
               }
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function scanFloors(scene:Object) : void
      {
         var mapEntries:Object;
         var floorCount:int;
         var entry:Object;
         var floorObj:Object;
         try
         {
            mapEntries = scene.mapEntries;
            if(!mapEntries)
            {
               return;
            }
            floorCount = 0;
            for each(entry in mapEntries)
            {
               if(!(!entry || entry.type != "floor"))
               {
                  floorCount++;
                  floorObj = {
                     "obj":entry,
                     "clip":entry.clip,
                     "type":"floor",
                     "name":entry.definition || "floor_" + entry.x + "_" + entry.z,
                     "definition":entry.definition,
                     "gridX":entry.x,
                     "gridY":entry.z,
                     "gridZ":entry.y,
                     "visible":(!!entry.clip ? entry.clip.visible : false),
                     "hasClip":entry.clip != null,
                     "thumbnail":(!!entry.clip ? createThumbnail(entry.clip) : null)
                  };
                  _realObjects.push(floorObj);
               }
            }
            info("OBJECTS","Found " + floorCount + " floors");
         }
         catch(e:Error)
         {
         }
      }
      
      private static function scanWalls(scene:Object) : void
      {
         var mapEntries:Object;
         var wallCount:int;
         var entry:Object;
         var wallObj:Object;
         try
         {
            mapEntries = scene.mapEntries;
            if(!mapEntries)
            {
               return;
            }
            wallCount = 0;
            for each(entry in mapEntries)
            {
               if(!(!entry || entry.type != "wall"))
               {
                  wallCount++;
                  wallObj = {
                     "obj":entry,
                     "clip":entry.clip,
                     "type":"wall",
                     "name":entry.definition || "wall_" + entry.x + "_" + entry.z,
                     "definition":entry.definition,
                     "gridX":entry.x,
                     "gridY":entry.z,
                     "gridZ":entry.y,
                     "visible":(!!entry.clip ? entry.clip.visible : false),
                     "hasClip":entry.clip != null,
                     "thumbnail":(!!entry.clip ? createThumbnail(entry.clip) : null)
                  };
                  _realObjects.push(wallObj);
               }
            }
            info("OBJECTS","Found " + wallCount + " walls");
         }
         catch(e:Error)
         {
         }
      }
      
      private static function createThumbnail(clip:DisplayObject) : Bitmap
      {
         var bounds:Rectangle;
         var scale:Number;
         var matrix:Matrix;
         var bmd:BitmapData;
         if(!clip)
         {
            return null;
         }
         try
         {
            bounds = clip.getBounds(clip);
            if(bounds.width == 0 || bounds.height == 0)
            {
               return null;
            }
            scale = Math.min(40 / bounds.width,40 / bounds.height);
            matrix = new Matrix();
            matrix.scale(scale,scale);
            matrix.translate(-bounds.x * scale,-bounds.y * scale);
            bmd = new BitmapData(40,40,true,0);
            bmd.draw(clip,matrix,null,null,null,true);
            return new Bitmap(bmd,"auto",true);
         }
         catch(e:Error)
         {
            return null;
         }
      }
      
      private static function getSanalikaInstance() : Object
      {
         try
         {
            if(_root.hasOwnProperty("sanalika"))
            {
               return _root["sanalika"];
            }
            try
            {
               return Sanalika.instance;
            }
            catch(e:Error)
            {
            }
         }
         catch(e:Error)
         {
         }
         return null;
      }
      
      private static function setMode(mode:String) : void
      {
         _directManipulationMode = mode;
         info("SYSTEM","Mode: " + mode);
         if(mode == "drag")
         {
            enableDragMode();
         }
         else
         {
            disableDragMode();
         }
      }
      
      private static function enableDragMode() : void
      {
         if(_root.stage)
         {
            _root.stage.addEventListener(MouseEvent.MOUSE_DOWN,onDragStart,false,0,true);
            _root.stage.addEventListener(MouseEvent.MOUSE_UP,onDragEnd,false,0,true);
            _root.stage.addEventListener(MouseEvent.MOUSE_MOVE,onDragMove,false,0,true);
         }
      }
      
      private static function disableDragMode() : void
      {
         if(_root.stage)
         {
            _root.stage.removeEventListener(MouseEvent.MOUSE_DOWN,onDragStart);
            _root.stage.removeEventListener(MouseEvent.MOUSE_UP,onDragEnd);
            _root.stage.removeEventListener(MouseEvent.MOUSE_MOVE,onDragMove);
         }
      }
      
      private static function onDragStart(e:MouseEvent) : void
      {
         if(_mainPanel && _mainPanel.hitTestPoint(e.stageX,e.stageY,true))
         {
            return;
         }
         for each(var objData in _realObjects)
         {
            if(!(!objData.clip || !objData.visible))
            {
               if(objData.clip.hitTestPoint(e.stageX,e.stageY,true))
               {
                  _isDraggingObject = true;
                  _selectedRealObject = objData;
                  _dragOffset.x = e.stageX - objData.clip.x;
                  _dragOffset.y = e.stageY - objData.clip.y;
                  createHighlight(objData.clip);
                  updateCoordsDisplay(objData);
                  info("DRAG","Dragging: " + objData.name);
                  return;
               }
            }
         }
      }
      
      private static function onDragMove(e:MouseEvent) : void
      {
         if(_isDraggingObject && _selectedRealObject && _selectedRealObject.clip)
         {
            var newX:Number = e.stageX - _dragOffset.x;
            var newY:Number = e.stageY - _dragOffset.y;
            _selectedRealObject.clip.x = newX;
            _selectedRealObject.clip.y = newY;
            if(_highlightSprite)
            {
               _highlightSprite.x = newX;
               _highlightSprite.y = newY;
            }
            updateCoordsDisplay(_selectedRealObject,newX,newY);
         }
      }
      
      private static function onDragEnd(e:MouseEvent) : void
      {
         var sanalika:Object;
         var scene:Object;
         var gridPos:Object;
         if(_isDraggingObject && _selectedRealObject)
         {
            try
            {
               sanalika = getSanalikaInstance();
               if(sanalika && sanalika.engine && sanalika.engine.scene)
               {
                  scene = sanalika.engine.scene;
                  gridPos = scene.translateToGrid(_selectedRealObject.clip.x,_selectedRealObject.clip.y);
                  if(gridPos)
                  {
                     _selectedRealObject.gridX = gridPos.x;
                     _selectedRealObject.gridY = gridPos.y;
                     if(_selectedRealObject.obj)
                     {
                        if(_selectedRealObject.obj.hasOwnProperty("x"))
                        {
                           _selectedRealObject.obj.x = gridPos.x;
                        }
                        if(_selectedRealObject.obj.hasOwnProperty("z"))
                        {
                           _selectedRealObject.obj.z = gridPos.y;
                        }
                     }
                     updateCoordsDisplay(_selectedRealObject);
                     info("DRAG","New grid: (" + gridPos.x + ", " + gridPos.y + ")");
                  }
               }
            }
            catch(e:Error)
            {
            }
            _isDraggingObject = false;
            showPropertiesPanel(_selectedRealObject);
         }
      }
      
      private static function updateCoordsDisplay(objData:Object, screenX:Number = -1, screenY:Number = -1) : void
      {
         if(!_coordsDisplay)
         {
            return;
         }
         var text:String = "📍 " + objData.name + " | Grid: (" + objData.gridX + ", " + objData.gridY + ")";
         if(screenX >= 0)
         {
            text += " | Screen: (" + Math.round(screenX) + ", " + Math.round(screenY) + ")";
         }
         _coordsDisplay.text = text;
      }
      
      private static function selectObject(objData:Object) : void
      {
         _selectedRealObject = objData;
         createHighlight(objData.clip);
         updateCoordsDisplay(objData);
         info("SYSTEM","Selected: " + objData.name);
         updateObjectsList();
         showPropertiesPanel(objData);
      }
      
      private static function showPropertiesPanel(objData:Object) : void
      {
         var bg:Shape;
         var title:TextField;
         var yPos:Number;
         var xInput:TextField;
         var yInput:TextField;
         var zInput:TextField;
         var dirInput:TextField;
         var visBtn:Sprite;
         var applyBtn:Sprite;
         var teleportBtn:Sprite;
         var deleteBtn:Sprite;
         var closeBtn:Sprite;
         closePropertiesPanel();
         if(!objData)
         {
            return;
         }
         _propertiesPanel = new Sprite();
         _propertiesPanel.name = "PropertiesPanel";
         _propertiesPanel.x = PANEL_WIDTH * _panelScale + 15;
         _propertiesPanel.y = 100;
         bg = new Shape();
         bg.graphics.beginFill(PANEL_DARK,0.98);
         bg.graphics.drawRoundRect(0,0,320,450,8,8);
         bg.graphics.endFill();
         _propertiesPanel.addChild(bg);
         title = createText("⚙️ " + objData.name.substr(0,18),12,12,296,16,true);
         _propertiesPanel.addChild(title);
         yPos = 45;
         addPropertyLabel("Type:",objData.type,12,yPos,_propertiesPanel);
         yPos += 32;
         addPropertyLabel("Grid X:",String(objData.gridX),12,yPos,_propertiesPanel);
         xInput = addPropertyInput(String(objData.gridX),170,yPos,_propertiesPanel);
         xInput.name = "gridX";
         yPos += 32;
         addPropertyLabel("Grid Y:",String(objData.gridY),12,yPos,_propertiesPanel);
         yInput = addPropertyInput(String(objData.gridY),170,yPos,_propertiesPanel);
         yInput.name = "gridY";
         yPos += 32;
         if(objData.hasOwnProperty("gridZ"))
         {
            addPropertyLabel("Grid Z:",String(objData.gridZ),12,yPos,_propertiesPanel);
            zInput = addPropertyInput(String(objData.gridZ),170,yPos,_propertiesPanel);
            zInput.name = "gridZ";
            yPos += 32;
         }
         if(objData.hasOwnProperty("dir"))
         {
            addPropertyLabel("Direction:",String(objData.dir),12,yPos,_propertiesPanel);
            dirInput = addPropertyInput(String(objData.dir),170,yPos,_propertiesPanel);
            dirInput.name = "dir";
            yPos += 32;
         }
         addPropertyLabel("Visible:",!!objData.visible ? "Yes" : "No",12,yPos,_propertiesPanel);
         visBtn = createSmallButton(!!objData.visible ? "Hide" : "Show",170,yPos - 3,70,24,function():void
         {
            if(objData.visible)
            {
               hideObject(objData);
            }
            else
            {
               showObject(objData);
            }
            showPropertiesPanel(objData);
         },ACCENT_COLOR);
         _propertiesPanel.addChild(visBtn);
         yPos += 38;
         addPropertyLabel("Rendered:",!!objData.hasClip ? "Yes" : "No",12,yPos,_propertiesPanel);
         yPos += 38;
         applyBtn = createSmallButton("✅ Apply Changes",12,yPos,296,35,function():void
         {
            applyPropertyChanges(objData,_propertiesPanel);
         },SUCCESS_COLOR);
         _propertiesPanel.addChild(applyBtn);
         yPos += 45;
         teleportBtn = createSmallButton("📍 Teleport",12,yPos,145,32,function():void
         {
            teleportToObject(objData);
         },HIGHLIGHT_COLOR);
         _propertiesPanel.addChild(teleportBtn);
         deleteBtn = createSmallButton("🗑️ Delete",163,yPos,145,32,function():void
         {
            deleteObject(objData);
            closePropertiesPanel();
         },ERROR_COLOR);
         _propertiesPanel.addChild(deleteBtn);
         yPos += 42;
         closeBtn = createSmallButton("❌ Close",12,yPos,296,28,closePropertiesPanel,ERROR_COLOR);
         _propertiesPanel.addChild(closeBtn);
         _mainPanel.addChild(_propertiesPanel);
      }
      
      private static function addPropertyLabel(label:String, value:String, x:Number, y:Number, parent:Sprite) : void
      {
         var labelTf:TextField = createText(label,x,y,150,13,true);
         parent.addChild(labelTf);
         var valueTf:TextField = createText(value,x + 90,y,150,13,false);
         valueTf.textColor = 16777215;
         parent.addChild(valueTf);
      }
      
      private static function addPropertyInput(value:String, x:Number, y:Number, parent:Sprite) : TextField
      {
         var input:TextField = new TextField();
         input.x = x;
         input.y = y;
         input.width = 138;
         input.height = 22;
         input.type = "input";
         input.border = true;
         input.borderColor = ACCENT_COLOR;
         input.background = true;
         input.backgroundColor = 657930;
         input.textColor = 16777215;
         input.text = value;
         var fmt:TextFormat = new TextFormat("_sans",12,16777215,true);
         input.defaultTextFormat = fmt;
         input.setTextFormat(fmt);
         parent.addChild(input);
         return input;
      }
      
      private static function applyPropertyChanges(objData:Object, panel:Sprite) : void
      {
         var xInput:TextField;
         var yInput:TextField;
         var zInput:TextField;
         var dirInput:TextField;
         var newX:int;
         var newY:int;
         var newZ:int;
         var newDir:int;
         try
         {
            xInput = panel.getChildByName("gridX") as TextField;
            yInput = panel.getChildByName("gridY") as TextField;
            zInput = panel.getChildByName("gridZ") as TextField;
            dirInput = panel.getChildByName("dir") as TextField;
            if(xInput && xInput.text != "")
            {
               newX = int(parseInt(xInput.text));
               objData.gridX = newX;
               if(objData.obj && objData.obj.hasOwnProperty("x"))
               {
                  objData.obj.x = newX;
               }
            }
            if(yInput && yInput.text != "")
            {
               newY = int(parseInt(yInput.text));
               objData.gridY = newY;
               if(objData.obj && objData.obj.hasOwnProperty("z"))
               {
                  objData.obj.z = newY;
               }
            }
            if(zInput && zInput.text != "")
            {
               newZ = int(parseInt(zInput.text));
               objData.gridZ = newZ;
               if(objData.obj && objData.obj.hasOwnProperty("y"))
               {
                  objData.obj.y = newZ;
               }
            }
            if(dirInput && dirInput.text != "")
            {
               newDir = int(parseInt(dirInput.text));
               objData.dir = newDir;
               if(objData.obj && objData.obj.hasOwnProperty("dir"))
               {
                  objData.obj.dir = newDir;
               }
            }
            updateObjectScreenPosition(objData);
            updateCoordsDisplay(objData);
            info("SYSTEM","✅ Applied");
            updateObjectsList();
         }
         catch(e:Error)
         {
            error("SYSTEM","Apply failed: " + e.message);
         }
      }
      
      private static function updateObjectScreenPosition(objData:Object) : void
      {
         var sanalika:Object;
         var scene:Object;
         var screenPos:Object;
         try
         {
            sanalika = getSanalikaInstance();
            if(!sanalika || !sanalika.engine || !sanalika.engine.scene)
            {
               return;
            }
            scene = sanalika.engine.scene;
            screenPos = scene.getPosFromGrid(objData.gridX,objData.gridY);
            if(screenPos && objData.clip)
            {
               objData.clip.x = screenPos.x;
               objData.clip.y = screenPos.y;
               if(_highlightSprite)
               {
                  _highlightSprite.x = screenPos.x;
                  _highlightSprite.y = screenPos.y;
               }
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function forceShowObject(objData:Object) : void
      {
         if(!objData)
         {
            return;
         }
         try
         {
            if(objData.clip)
            {
               objData.clip.visible = true;
               objData.visible = true;
               info("SYSTEM","👁️+ Force shown");
            }
            else
            {
               warn("SYSTEM","No clip to show - needs rendering");
            }
            updateObjectsList();
         }
         catch(e:Error)
         {
         }
      }
      
      private static function cloneObject(objData:Object) : void
      {
         var sanalika:Object;
         var scene:Object;
         var clonedObj:Object;
         var newClip:DisplayObject;
         var screenPos:Object;
         if(!objData)
         {
            return;
         }
         try
         {
            info("SYSTEM","📋 Cloning: " + objData.name);
            sanalika = getSanalikaInstance();
            if(!sanalika || !sanalika.engine || !sanalika.engine.scene)
            {
               return;
            }
            scene = sanalika.engine.scene;
            clonedObj = {
               "obj":null,
               "clip":null,
               "type":objData.type,
               "name":objData.name + "_copy",
               "definition":objData.definition,
               "gridX":objData.gridX + 1,
               "gridY":objData.gridY + 1,
               "gridZ":objData.gridZ,
               "width":objData.width,
               "height":objData.height,
               "dir":objData.dir,
               "visible":true,
               "hasClip":false,
               "thumbnail":objData.thumbnail
            };
            if(objData.definition)
            {
               try
               {
                  newClip = scene.getMovieClip(objData.definition);
                  if(newClip)
                  {
                     screenPos = scene.getPosFromGrid(clonedObj.gridX,clonedObj.gridY);
                     newClip.x = screenPos.x;
                     newClip.y = screenPos.y;
                     if(objData.type == "floor" && scene.floorContainer)
                     {
                        scene.floorContainer.addChild(newClip);
                     }
                     else if(scene.elementsContainer)
                     {
                        scene.elementsContainer.addChild(newClip);
                     }
                     clonedObj.clip = newClip;
                     clonedObj.hasClip = true;
                     info("SYSTEM","✅ Cloned with new clip");
                  }
                  else
                  {
                     warn("SYSTEM","getMovieClip returned null for: " + objData.definition);
                  }
               }
               catch(e:Error)
               {
                  warn("SYSTEM","Could not create clip: " + e.message);
               }
            }
            _realObjects.push(clonedObj);
            updateObjectsList();
            updateStats();
            info("SYSTEM","✅ Clone added at (" + clonedObj.gridX + ", " + clonedObj.gridY + ")");
         }
         catch(e:Error)
         {
            error("SYSTEM","Clone failed: " + e.message);
         }
      }
      
      private static function startDragFromPanel(objData:Object, e:MouseEvent) : void
      {
         info("DRAG","🖱️ Drag from panel started: " + objData.name);
         _isDraggingFromPanel = true;
         _draggedItemData = objData;
         createDragPreview(objData,e.stageX,e.stageY);
         if(_root.stage)
         {
            _root.stage.addEventListener(MouseEvent.MOUSE_MOVE,onDragFromPanelMove);
            _root.stage.addEventListener(MouseEvent.MOUSE_UP,onDragFromPanelEnd);
         }
      }
      
      private static function createDragPreview(objData:Object, x:Number, y:Number) : void
      {
         removeDragPreview();
         _dragPreview = new Sprite();
         _dragPreview.mouseEnabled = false;
         _dragPreview.mouseChildren = false;
         var bg:Shape = new Shape();
         bg.graphics.beginFill(HIGHLIGHT_COLOR,0.8);
         bg.graphics.drawRoundRect(0,0,80,80,8,8);
         bg.graphics.endFill();
         bg.graphics.lineStyle(3,SUCCESS_COLOR,1);
         bg.graphics.drawRoundRect(0,0,80,80,8,8);
         _dragPreview.addChild(bg);
         if(objData.thumbnail)
         {
            var thumb:Bitmap = new Bitmap(objData.thumbnail.bitmapData);
            thumb.x = 20;
            thumb.y = 20;
            thumb.width = 40;
            thumb.height = 40;
            thumb.smoothing = true;
            _dragPreview.addChild(thumb);
         }
         else
         {
            var icon:TextField = createText(getTypeIcon(objData.type),20,25,40,30,false);
            icon.mouseEnabled = false;
            _dragPreview.addChild(icon);
         }
         var label:TextField = createText(objData.name.substr(0,10),0,62,80,11,true);
         label.mouseEnabled = false;
         _dragPreview.addChild(label);
         _dragPreview.x = x - 40;
         _dragPreview.y = y - 40;
         _root.stage.addChild(_dragPreview);
      }
      
      private static function onDragFromPanelMove(e:MouseEvent) : void
      {
         if(_dragPreview)
         {
            _dragPreview.x = e.stageX - 40;
            _dragPreview.y = e.stageY - 40;
         }
      }
      
      private static function onDragFromPanelEnd(e:MouseEvent) : void
      {
         var sanalika:Object;
         var scene:Object;
         var gridPos:Object;
         var newObj:Object;
         var newClip:DisplayObject;
         var screenPos:Object;
         if(!_isDraggingFromPanel || !_draggedItemData)
         {
            return;
         }
         if(_mainPanel && _mainPanel.hitTestPoint(e.stageX,e.stageY,true))
         {
            info("DRAG","❌ Dropped on panel - cancelled");
            removeDragPreview();
            _isDraggingFromPanel = false;
            _draggedItemData = null;
            return;
         }
         try
         {
            sanalika = getSanalikaInstance();
            if(sanalika && sanalika.engine && sanalika.engine.scene)
            {
               scene = sanalika.engine.scene;
               gridPos = scene.translateToGrid(e.stageX,e.stageY);
               if(gridPos)
               {
                  info("DRAG","✅ Placing at grid: (" + gridPos.x + ", " + gridPos.y + ")");
                  newObj = {
                     "obj":null,
                     "clip":null,
                     "type":_draggedItemData.type,
                     "name":_draggedItemData.name + "_placed",
                     "definition":_draggedItemData.definition,
                     "gridX":gridPos.x,
                     "gridY":gridPos.y,
                     "gridZ":0,
                     "width":_draggedItemData.width,
                     "height":_draggedItemData.height,
                     "dir":_draggedItemData.dir,
                     "visible":true,
                     "hasClip":false,
                     "thumbnail":_draggedItemData.thumbnail
                  };
                  if(_draggedItemData.definition)
                  {
                     try
                     {
                        newClip = scene.getMovieClip(_draggedItemData.definition);
                        if(newClip)
                        {
                           screenPos = scene.getPosFromGrid(gridPos.x,gridPos.y);
                           newClip.x = screenPos.x;
                           newClip.y = screenPos.y;
                           if(_draggedItemData.type == "floor" && scene.floorContainer)
                           {
                              scene.floorContainer.addChild(newClip);
                           }
                           else if(scene.elementsContainer)
                           {
                              scene.elementsContainer.addChild(newClip);
                           }
                           newObj.clip = newClip;
                           newObj.hasClip = true;
                           newObj.thumbnail = createThumbnail(newClip);
                           info("DRAG","✅ Clip created and added to scene");
                        }
                        else
                        {
                           warn("DRAG","getMovieClip returned null for: " + _draggedItemData.definition);
                        }
                     }
                     catch(e:Error)
                     {
                        warn("DRAG","Could not create clip: " + e.message);
                     }
                  }
                  _realObjects.push(newObj);
                  updateObjectsList();
                  updateStats();
                  info("SYSTEM","🎉 Object placed in game!");
               }
            }
         }
         catch(e:Error)
         {
            error("DRAG","Place failed: " + e.message);
         }
         removeDragPreview();
         _isDraggingFromPanel = false;
         _draggedItemData = null;
         if(_root.stage)
         {
            _root.stage.removeEventListener(MouseEvent.MOUSE_MOVE,onDragFromPanelMove);
            _root.stage.removeEventListener(MouseEvent.MOUSE_UP,onDragFromPanelEnd);
         }
      }
      
      private static function removeDragPreview() : void
      {
         if(_dragPreview && _dragPreview.parent)
         {
            try
            {
               _dragPreview.parent.removeChild(_dragPreview);
            }
            catch(e:Error)
            {
            }
         }
         _dragPreview = null;
      }
      
      private static function teleportToObject(objData:Object) : void
      {
         var sanalika:Object;
         var scene:Object;
         var charComponent:Object;
         try
         {
            sanalika = getSanalikaInstance();
            if(!sanalika || !sanalika.engine || !sanalika.engine.scene)
            {
               return;
            }
            scene = sanalika.engine.scene;
            charComponent = scene.getComponent(SceneCharacterComponent);
            if(charComponent && charComponent.myChar)
            {
               charComponent.myChar.walkRequest(objData.gridX,objData.gridY);
               info("SYSTEM","🚶 Walking to object");
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function closePropertiesPanel() : void
      {
         if(_propertiesPanel && _propertiesPanel.parent)
         {
            _propertiesPanel.parent.removeChild(_propertiesPanel);
         }
         _propertiesPanel = null;
      }
      
      private static function createHighlight(clip:DisplayObject) : void
      {
         var bounds:Rectangle;
         removeHighlight();
         if(!_showHighlights || !clip)
         {
            return;
         }
         try
         {
            _highlightSprite = new Sprite();
            _highlightSprite.mouseEnabled = false;
            bounds = clip.getBounds(clip.parent);
            _highlightSprite.graphics.lineStyle(4,SUCCESS_COLOR,0.9);
            _highlightSprite.graphics.drawRect(bounds.x - 3,bounds.y - 3,bounds.width + 6,bounds.height + 6);
            _highlightSprite.x = clip.x;
            _highlightSprite.y = clip.y;
            if(clip.parent)
            {
               clip.parent.addChild(_highlightSprite);
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function removeHighlight() : void
      {
         if(_highlightSprite && _highlightSprite.parent)
         {
            try
            {
               _highlightSprite.parent.removeChild(_highlightSprite);
            }
            catch(e:Error)
            {
            }
         }
         _highlightSprite = null;
      }
      
      private static function hideObject(objData:Object) : void
      {
         if(!objData || !objData.clip)
         {
            return;
         }
         try
         {
            objData.clip.visible = false;
            objData.visible = false;
            info("SYSTEM","🙈 Hidden");
            updateObjectsList();
         }
         catch(e:Error)
         {
         }
      }
      
      private static function showObject(objData:Object) : void
      {
         if(!objData || !objData.clip)
         {
            return;
         }
         try
         {
            objData.clip.visible = true;
            objData.visible = true;
            info("SYSTEM","👁️ Shown");
            updateObjectsList();
         }
         catch(e:Error)
         {
         }
      }
      
      private static function deleteObject(objData:Object) : void
      {
         var index:int;
         if(!objData || !objData.clip)
         {
            return;
         }
         try
         {
            if(objData.clip.parent)
            {
               objData.clip.parent.removeChild(objData.clip);
            }
            index = _realObjects.indexOf(objData);
            if(index >= 0)
            {
               _realObjects.splice(index,1);
            }
            if(_selectedRealObject == objData)
            {
               _selectedRealObject = null;
               removeHighlight();
            }
            info("SYSTEM","🗑️ Deleted");
            updateObjectsList();
            updateStats();
         }
         catch(e:Error)
         {
         }
      }
      
      private static function clearAllHighlights() : void
      {
         removeHighlight();
         _selectedRealObject = null;
         if(_coordsDisplay)
         {
            _coordsDisplay.text = "📍 Position: --";
         }
         info("SYSTEM","Cleared");
      }
      
      private static function refreshView() : void
      {
         updateObjectsList();
         updateStats();
         info("SYSTEM","Refreshed");
      }
      
      private static function updateObjectsList() : void
      {
         var content:Sprite = _objectsListPanel.getChildByName("content") as Sprite;
         if(!content)
         {
            return;
         }
         while(content.numChildren > 0)
         {
            content.removeChildAt(0);
         }
         var yPos:Number = -_scrollPosition;
         var itemHeight:Number = 52;
         for each(var objData in _realObjects)
         {
            var item:Sprite = createObjectListItem(objData,yPos);
            content.addChild(item);
            yPos += itemHeight;
         }
         _maxScroll = Math.max(0,_realObjects.length * itemHeight - 290);
         if(_realObjects.length == 0)
         {
            var emptyMsg:TextField = createText("No objects. Click SCAN",10,10,300,14,false);
            emptyMsg.textColor = 8359053;
            content.addChild(emptyMsg);
         }
         updateScrollbar();
      }
      
      private static function updateScrollbar() : void
      {
         var scrollbar:Sprite = _objectsListPanel.getChildByName("scrollbar") as Sprite;
         if(!scrollbar)
         {
            return;
         }
         var thumb:Sprite = scrollbar.getChildByName("thumb") as Sprite;
         if(!thumb)
         {
            return;
         }
         if(_maxScroll <= 0)
         {
            thumb.visible = false;
            return;
         }
         thumb.visible = true;
         var thumbHeight:Number = Math.max(30,290 * (290 / (_maxScroll + 290)));
         thumb.graphics.clear();
         thumb.graphics.beginFill(ACCENT_COLOR,0.9);
         thumb.graphics.drawRoundRect(0,0,15,thumbHeight,5,5);
         thumb.graphics.endFill();
         var thumbPos:Number = _scrollPosition / _maxScroll * (290 - thumbHeight);
         thumb.y = thumbPos;
      }
      
      private static function createObjectListItem(objData:Object, y:Number) : Sprite
      {
         var isSelected:Boolean;
         var bgColor:uint;
         var bgAlpha:Number;
         var bg:Shape;
         var thumbnailContainer:Sprite;
         var placeholderBg:Shape;
         var icon:String;
         var iconTf:TextField;
         var dragIcon:TextField;
         var nameTf:TextField;
         var typeTf:TextField;
         var posTf:TextField;
         var statusText:String;
         var statusColor:uint;
         var statusTf:TextField;
         var editBtn:Sprite;
         var visIcon:String;
         var visBtn:Sprite;
         var selectBtn:Sprite;
         var deleteBtn:Sprite;
         var cloneBtn:Sprite;
         var teleBtn:Sprite;
         var showBtn:Sprite;
         var dragInfoBtn:Sprite;
         var item:Sprite = new Sprite();
         item.y = y;
         item.buttonMode = true;
         isSelected = _selectedRealObject == objData;
         bgColor = PANEL_BG;
         bgAlpha = 0.7;
         if(!objData.hasClip)
         {
            bgColor = 3807770;
            bgAlpha = 0.8;
         }
         else if(!objData.visible)
         {
            bgColor = 1710634;
            bgAlpha = 0.6;
         }
         if(isSelected)
         {
            bgColor = HIGHLIGHT_COLOR;
            bgAlpha = 0.9;
         }
         bg = new Shape();
         bg.graphics.beginFill(bgColor,bgAlpha);
         bg.graphics.drawRoundRect(0,0,PANEL_WIDTH - 50,48,6,6);
         bg.graphics.endFill();
         if(!objData.visible && objData.hasClip)
         {
            bg.graphics.lineStyle(2,16750592,0.8);
            bg.graphics.drawRoundRect(0,0,PANEL_WIDTH - 50,48,6,6);
         }
         else if(!objData.hasClip)
         {
            bg.graphics.lineStyle(2,ERROR_COLOR,0.8);
            bg.graphics.drawRoundRect(0,0,PANEL_WIDTH - 50,48,6,6);
         }
         item.addChild(bg);
         thumbnailContainer = new Sprite();
         thumbnailContainer.x = 6;
         thumbnailContainer.y = 4;
         if(objData.thumbnail)
         {
            objData.thumbnail.x = 0;
            objData.thumbnail.y = 0;
            if(!objData.visible)
            {
               objData.thumbnail.alpha = 0.4;
            }
            thumbnailContainer.addChild(objData.thumbnail);
         }
         else
         {
            placeholderBg = new Shape();
            placeholderBg.graphics.beginFill(!!objData.hasClip ? 2763326 : 3807770);
            placeholderBg.graphics.drawRect(0,0,40,40);
            placeholderBg.graphics.endFill();
            thumbnailContainer.addChild(placeholderBg);
            icon = getTypeIcon(objData.type);
            iconTf = createText(icon,0,10,40,20,false);
            iconTf.mouseEnabled = false;
            if(!objData.visible)
            {
               iconTf.alpha = 0.4;
            }
            thumbnailContainer.addChild(iconTf);
         }
         dragIcon = createText("✋",24,24,20,14,false);
         dragIcon.mouseEnabled = false;
         dragIcon.alpha = 0.7;
         thumbnailContainer.addChild(dragIcon);
         thumbnailContainer.buttonMode = true;
         thumbnailContainer.addEventListener(MouseEvent.MOUSE_DOWN,function(e:MouseEvent):void
         {
            startDragFromPanel(objData,e);
            e.stopPropagation();
         });
         item.addChild(thumbnailContainer);
         nameTf = createText(objData.name.substr(0,14),52,6,140,13,true);
         if(!objData.visible)
         {
            nameTf.alpha = 0.6;
         }
         item.addChild(nameTf);
         typeTf = createText(objData.type,52,22,70,11,false);
         typeTf.textColor = 12313082;
         if(!objData.visible)
         {
            typeTf.alpha = 0.6;
         }
         item.addChild(typeTf);
         posTf = createText("(" + objData.gridX + "," + objData.gridY + ")",52,34,100,10,false);
         posTf.textColor = 8359053;
         if(!objData.visible)
         {
            posTf.alpha = 0.6;
         }
         item.addChild(posTf);
         statusText = "";
         statusColor = SUCCESS_COLOR;
         if(!objData.hasClip)
         {
            statusText = "❌ NO CLIP";
            statusColor = ERROR_COLOR;
         }
         else if(!objData.visible)
         {
            statusText = "🙈 HIDDEN";
            statusColor = 16750592;
         }
         else
         {
            statusText = "✅ VISIBLE";
            statusColor = SUCCESS_COLOR;
         }
         statusTf = createText(statusText,155,16,70,11,true);
         statusTf.textColor = statusColor;
         item.addChild(statusTf);
         editBtn = createTinyButton("⚙️",PANEL_WIDTH - 185,6,28,18,function():void
         {
            selectObject(objData);
         },ACCENT_COLOR);
         item.addChild(editBtn);
         visIcon = !!objData.visible ? "👁️" : "🙈";
         visBtn = createTinyButton(visIcon,PANEL_WIDTH - 152,6,28,18,function():void
         {
            if(objData.visible)
            {
               hideObject(objData);
            }
            else
            {
               showObject(objData);
            }
         },ACCENT_COLOR);
         item.addChild(visBtn);
         selectBtn = createTinyButton("🎯",PANEL_WIDTH - 119,6,28,18,function():void
         {
            selectObject(objData);
         },SUCCESS_COLOR);
         item.addChild(selectBtn);
         deleteBtn = createTinyButton("🗑️",PANEL_WIDTH - 86,6,28,18,function():void
         {
            deleteObject(objData);
         },ERROR_COLOR);
         item.addChild(deleteBtn);
         cloneBtn = createTinyButton("📋",PANEL_WIDTH - 185,28,28,18,function():void
         {
            cloneObject(objData);
         },SUCCESS_COLOR);
         item.addChild(cloneBtn);
         teleBtn = createTinyButton("📍",PANEL_WIDTH - 152,28,28,18,function():void
         {
            teleportToObject(objData);
         },HIGHLIGHT_COLOR);
         item.addChild(teleBtn);
         showBtn = createTinyButton("👁️+",PANEL_WIDTH - 119,28,28,18,function():void
         {
            forceShowObject(objData);
         },SUCCESS_COLOR);
         item.addChild(showBtn);
         dragInfoBtn = createTinyButton("✋",PANEL_WIDTH - 86,28,28,18,function():void
         {
            info("SYSTEM","Drag thumbnail to game!");
         },10233776);
         item.addChild(dragInfoBtn);
         item.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void
         {
            selectObject(objData);
         });
         return item;
      }
      
      private static function getTypeIcon(type:String) : String
      {
         switch(type)
         {
            case "bot":
               return "🤖";
            case "user":
               return "👤";
            case "chair":
               return "🪑";
            case "floor":
               return "🟩";
            case "door":
               return "🚪";
            case "wall":
               return "🧱";
            default:
               return "📦";
         }
      }
      
      private static function updateStats() : void
      {
         if(!_statsText)
         {
            return;
         }
         var bots:int = 0;
         var users:int = 0;
         var furniture:int = 0;
         for each(var obj in _realObjects)
         {
            switch(obj.type)
            {
               case "bot":
                  bots++;
                  break;
               case "user":
                  users++;
                  break;
               default:
                  furniture++;
                  break;
            }
         }
         _statsText.text = "Objects: " + _realObjects.length + " | Bots: " + bots + " | Users: " + users + " | Furn: " + furniture;
      }
      
      private static function addConsoleLog(channel:String, level:String, message:String) : void
      {
         var timestamp:String = new Date().toLocaleTimeString();
         _consoleLogs.unshift({
            "time":timestamp,
            "channel":channel,
            "level":level,
            "message":message
         });
         if(_consoleLogs.length > _maxConsoleLogs)
         {
            _consoleLogs.pop();
         }
         updateConsoleDisplay();
      }
      
      private static function updateConsoleDisplay() : void
      {
         var logDisplay:TextField;
         var text:String;
         var maxDisplay:int;
         var i:int;
         var log:Object;
         if(!_mainPanel || !_mainPanel.visible)
         {
            return;
         }
         try
         {
            logDisplay = _consoleLogPanel.getChildByName("logDisplay") as TextField;
            if(logDisplay)
            {
               text = "";
               maxDisplay = Math.min(12,_consoleLogs.length);
               i = 0;
               while(i < maxDisplay)
               {
                  log = _consoleLogs[i];
                  text += "[" + log.channel + "] " + log.message + "\n";
                  i++;
               }
               logDisplay.text = text;
            }
         }
         catch(e:Error)
         {
         }
      }
      
      private static function exportConsoleLogs() : void
      {
         var export:String = "=== LOGS ===\n\n";
         for each(var log in _consoleLogs)
         {
            export += "[" + log.time + "][" + log.channel + "] " + log.message + "\n";
         }
         System.setClipboard(export);
         info("SYSTEM","Exported");
      }
      
      private static function createText(text:String, x:Number, y:Number, width:Number, size:int = 12, bold:Boolean = false) : TextField
      {
         var tf:TextField = new TextField();
         var fmt:TextFormat = new TextFormat("_sans",size,TEXT_COLOR,bold);
         tf.defaultTextFormat = fmt;
         tf.text = text;
         tf.x = x;
         tf.y = y;
         tf.width = width;
         tf.height = size + 8;
         tf.selectable = false;
         tf.mouseEnabled = false;
         return tf;
      }
      
      private static function createSmallButton(label:String, x:Number, y:Number, width:Number, height:Number, action:Function, color:uint) : Sprite
      {
         var bg:Shape;
         var text:TextField;
         var btn:Sprite = new Sprite();
         btn.x = x;
         btn.y = y;
         btn.buttonMode = true;
         bg = new Shape();
         bg.graphics.beginFill(color,0.85);
         bg.graphics.drawRoundRect(0,0,width,height,4,4);
         bg.graphics.endFill();
         btn.addChild(bg);
         text = createText(label,0,(height - 14) / 2,width,11,true);
         text.autoSize = TextFieldAutoSize.CENTER;
         btn.addChild(text);
         btn.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void
         {
            action();
            e.stopPropagation();
         });
         btn.addEventListener(MouseEvent.MOUSE_OVER,function(e:MouseEvent):void
         {
            btn.scaleX = btn.scaleY = 1.05;
         });
         btn.addEventListener(MouseEvent.MOUSE_OUT,function(e:MouseEvent):void
         {
            btn.scaleX = btn.scaleY = 1;
         });
         return btn;
      }
      
      private static function createTinyButton(label:String, x:Number, y:Number, width:Number, height:Number, action:Function, color:uint) : Sprite
      {
         var text:TextField;
         var btn:Sprite = new Sprite();
         btn.x = x;
         btn.y = y;
         btn.buttonMode = true;
         btn.graphics.beginFill(color,0.8);
         btn.graphics.drawRoundRect(0,0,width,height,3,3);
         btn.graphics.endFill();
         text = createText(label,0,(height - 12) / 2,width,10,false);
         text.autoSize = TextFieldAutoSize.CENTER;
         btn.addChild(text);
         btn.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void
         {
            action();
            e.stopPropagation();
         });
         return btn;
      }
      
      private static function setupEventListeners() : void
      {
         if(_root.stage)
         {
            _root.stage.addEventListener(KeyboardEvent.KEY_DOWN,onKeyDown);
         }
      }
      
      private static function onKeyDown(e:KeyboardEvent) : void
      {
         switch(e.keyCode)
         {
            case 123:
               togglePanel();
               break;
            case 46:
               if(_selectedRealObject)
               {
                  deleteObject(_selectedRealObject);
               }
               break;
            case 82:
               if(e.ctrlKey)
               {
                  scanRealGameObjects();
               }
               break;
            case 72:
               if(_selectedRealObject)
               {
                  if(_selectedRealObject.visible)
                  {
                     hideObject(_selectedRealObject);
                  }
                  else
                  {
                     showObject(_selectedRealObject);
                  }
               }
               break;
            case 187:
               zoomIn();
               break;
            case 189:
               zoomOut();
         }
      }
      
      private static function togglePanel() : void
      {
         _isPanelVisible = !_isPanelVisible;
         _mainPanel.visible = _isPanelVisible;
         if(_isPanelVisible)
         {
            if(_mainPanel.parent)
            {
               _mainPanel.parent.setChildIndex(_mainPanel,_mainPanel.parent.numChildren - 1);
            }
            scanRealGameObjects();
         }
         else
         {
            removeHighlight();
         }
         info("SYSTEM","Panel " + (_isPanelVisible ? "shown" : "hidden"));
      }
      
      private static function positionPanel() : void
      {
         if(_mainPanel && _root.stage)
         {
            _mainPanel.x = _root.stage.stageWidth - PANEL_WIDTH * _panelScale - 20;
            _mainPanel.y = 50;
         }
      }
      
      public static function info(channel:String, ... args) : void
      {
         var msg:String = args.join(" ");
         addConsoleLog(channel,"INFO",msg);
         try
         {
            Cc.infoch(channel,msg);
         }
         catch(e:Error)
         {
            trace("[" + channel + "] " + msg);
         }
      }
      
      public static function warn(channel:String, ... args) : void
      {
         var msg:String = args.join(" ");
         addConsoleLog(channel,"WARN",msg);
         try
         {
            Cc.warnch(channel,msg);
         }
         catch(e:Error)
         {
            trace("[WARN][" + channel + "] " + msg);
         }
      }
      
      public static function error(channel:String, ... args) : void
      {
         var msg:String = args.join(" ");
         addConsoleLog(channel,"ERROR",msg);
         try
         {
            Cc.errorch(channel,msg);
         }
         catch(e:Error)
         {
            trace("[ERROR][" + channel + "] " + msg);
         }
      }
   }
}
