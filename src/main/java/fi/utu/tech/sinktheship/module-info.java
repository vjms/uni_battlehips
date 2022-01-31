// declarate module imports and exports here
// see: https://tech.utugit.fi/soft/tools/lectures/tko8971/build/jpms/

module fi.utu.tech.sinktheship {
  // requires java.desktop;
  requires transitive javafx.base;
  requires transitive javafx.fxml;
  requires transitive javafx.controls;
  requires transitive javafx.graphics;
  requires transitive javafx.media;
  requires transitive javafx.web;

  exports fi.utu.tech.sinktheship.gui.javafx;
  exports fi.utu.tech.sinktheship.gui.javafx.controllers;
  exports fi.utu.tech.sinktheship.gui.javafx.components;

  exports fi.utu.tech.sinktheship.network;
  exports fi.utu.tech.sinktheship.network.packet;

  exports fi.utu.tech.sinktheship.utility.events;
  exports fi.utu.tech.sinktheship.ships;
  exports fi.utu.tech.sinktheship.game;
  exports fi.utu.tech.sinktheship;

  opens fi.utu.tech.sinktheship.gui.javafx;
}
