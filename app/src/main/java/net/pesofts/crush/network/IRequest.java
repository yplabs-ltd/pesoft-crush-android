package net.pesofts.crush.network;

public interface IRequest {

	void execute();

	void cancel();

	boolean isFinished();
}
